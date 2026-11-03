package uz.davrbank.compliance.service.impl

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.Operator
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.util.ObjectBuilder
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uz.davrbank.compliance.config.ApplicationProperties
import uz.davrbank.compliance.dto.request.OrganizationSearchQuery
import uz.davrbank.compliance.dto.request.filter.BaseFilter
import uz.davrbank.compliance.dto.request.filter.OrgSearch
import uz.davrbank.compliance.dto.request.filter.OrgType
import uz.davrbank.compliance.dto.response.ElasticsearchSearchHit
import uz.davrbank.compliance.dto.response.ElasticsearchSearchResponse
import uz.davrbank.compliance.dto.response.settings.ElasticSearchRankRangeResponse
import uz.davrbank.compliance.enums.Type
import uz.davrbank.compliance.exception.ServiceUnavailableException
import uz.davrbank.compliance.service.ElasticsearchSearchService
import uz.davrbank.compliance.service.SettingsService
import uz.davrbank.compliance.util.SearchScoreScale
import uz.davrbank.compliance.util.TextNormalizer

@Service
class ElasticsearchSearchServiceImpl(
    private val elasticsearchClient: ElasticsearchClient,
    private val properties: ApplicationProperties,
    private val textNormalizer: TextNormalizer,
    private val settingsService: SettingsService,
) : ElasticsearchSearchService {

    override fun search(filter: BaseFilter<OrgSearch>): ElasticsearchSearchResponse {
        val currentTime: Long = System.currentTimeMillis()
        val searchCriteria = filter.params ?: OrgSearch()
        val pagination = requireNotNull(filter.pagination) { "Property 'pagination' must be provided." }
        val type = (searchCriteria.orgType ?: DEFAULT_ORG_TYPE).toSearchType()
        val effectivePageable = pagination.toPageable(properties.elastic.maxPageSize)
        val searchQuery = buildSearchQuery(
            raw = filter.keyword.orEmpty(),
            type = type,
            region = searchCriteria.region,
            birth = searchCriteria.birth,
        )

        if (searchQuery.normalized.isBlank()) {
            return emptyResponse(effectivePageable)
        }

        val rankRange = settingsService.getElasticSearchRankRange()
        val searchRequest = buildSearchRequest(type, searchQuery, effectivePageable, rankRange)

        logger.info {
            "Searching Elasticsearch for type='${type.name}', search='${searchQuery.raw.maskForLogs()}', " +
                    "page=${effectivePageable.pageNumber}, size=${effectivePageable.pageSize}"
        }

        val response = try {
            elasticsearchClient.search(
                searchRequest.request,
                Map::class.javaObjectType,
            )
        } catch (ex: Exception) {
            logger.error(ex) {
                "Elasticsearch search failed for type='${type.name}', search='${searchQuery.raw.maskForLogs()}'"
            }

            throw ServiceUnavailableException(
                message = "Elasticsearch search is currently unavailable.",
                code = "ELASTICSEARCH_UNAVAILABLE",
                cause = ex,
            )
        }

        val resp = response.toSearchResponse(effectivePageable, searchRequest.scoreScale, rankRange)

        logger.info {
            "-----------Request  completed in ${System.currentTimeMillis() - currentTime} ms -----------"
        }
        return resp
    }

    private fun buildSearchQuery(
        raw: String,
        type: Type,
        region: String?,
        birth: String?,
    ): OrganizationSearchQuery = OrganizationSearchQuery(
        raw = raw,
        normalized = textNormalizer.normalize(raw),
        tokens = textNormalizer.tokenize(raw),
        filters = buildSearchFilters(type, region, birth),
    )

    private fun buildSearchFilters(
        type: Type,
        region: String?,
        birth: String?,
    ): Map<String, String> = buildMap {
        put(TYPE_FIELD, type.name)
        region?.trim()?.takeIf { it.isNotBlank() }?.let { put(REGION_FIELD, it) }
        birth?.trim()?.takeIf { it.isNotBlank() }?.let { put(BIRTH_FIELD, it) }
    }

    private fun buildSearchRequest(
        type: Type,
        searchQuery: OrganizationSearchQuery,
        pageable: Pageable,
        rankRange: ElasticSearchRankRangeResponse,
    ): RankedSearchRequest = when (type) {
        Type.Organization -> buildOrganizationSearchRequest(searchQuery, pageable, rankRange)
        Type.Individual -> buildIndividualSearchRequest(searchQuery, pageable, rankRange)
        Type.BIG -> buildCodeSearchRequest(type, BIG_FIELD, searchQuery, pageable, rankRange)
        Type.SWIFT -> buildCodeSearchRequest(type, SWIFT_FIELD, searchQuery, pageable, rankRange)
        Type.Vessel -> buildCodeSearchRequest(type, SWIFT_FIELD, searchQuery, pageable, rankRange)
        Type.Aircraft -> buildCodeSearchRequest(type, SWIFT_FIELD, searchQuery, pageable, rankRange)
    }

    private fun buildOrganizationSearchRequest(
        searchQuery: OrganizationSearchQuery,
        pageable: Pageable,
        rankRange: ElasticSearchRankRangeResponse,
    ): RankedSearchRequest {
        val organizationTokens = searchQuery.normalized.toSearchTokens()
        val organizationName = organizationTokens.joinToString(" ")
        val identificationNumber = searchQuery.raw.organizationIdentificationNumberSearchValue()
        val shouldQueries = buildList {
            if (!searchQuery.raw.isIdentifierDominantSearch()) {
                addAll(buildOrganizationNameQueries(searchQuery.raw, organizationName, organizationTokens))
            }
            identificationNumber?.let {
                addAll(
                    buildConstantScoreMultiValueQueries(
                        fieldName = IDENTIFICATION_NUMBER_FIELD,
                        queryValue = it,
                        boost = IdentificationNumberMatchWeights.exactMatch,
                    )
                )
            }
        }.distinctBy { it.toString() }
        val scoreScaleMaxRawScore = ORGANIZATION_NAME_SCORE_SCALE_MAX_RAW_SCORE

        return RankedSearchRequest(
            request = SearchRequest.of { request ->
                request.index(properties.elastic.index)
                request.from(pageable.offset.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                request.size(pageable.pageSize)
                request.minScore(rankRange.minRawScore(scoreScaleMaxRawScore))
                request.applySearchPerformanceOptions()
                request.applyEntityGuidGrouping()
                request.source { source ->
                    source.filter { filter ->
                        filter.includes(sourceFields())
                    }
                }
                request.query { query ->
                    buildOrganizationBoolQuery(query, shouldQueries, searchQuery.filters)
                }
            },
            scoreScale = SearchScoreScale(scoreScaleMaxRawScore),
        )
    }

    private fun buildOrganizationNameQueries(
        rawKeyword: String,
        organizationName: String,
        organizationTokens: List<String>,
    ): List<Query> {
        if (organizationName.isBlank() || organizationTokens.isEmpty()) {
            return emptyList()
        }

        return buildList {
            add(buildConstantScoreQuery(buildOrganizationExactNameQuery(rawKeyword, organizationName), 100f))
            add(buildConstantScoreQuery(buildOrganizationPhraseQuery(organizationName), 80f))
            add(
                buildConstantScoreQuery(
                    buildOrganizationMultiMatchQuery(organizationName, TextQueryType.CrossFields, Operator.And),
                    50f,
                )
            )
            add(
                buildConstantScoreQuery(
                    buildOrganizationMultiMatchQuery(organizationName, TextQueryType.BestFields, Operator.Or),
                    10f,
                )
            )
            organizationTokens
                .filter { token -> token.length >= MIN_WILDCARD_TOKEN_LENGTH }
                .take(properties.elastic.maxWildcardTokens.coerceAtLeast(0))
                .forEach { token ->
                    add(buildConstantScoreQuery(buildOrganizationWildcardTokenQuery(token), 1f))
                }
        }
    }

    private fun buildOrganizationBoolQuery(
        query: Query.Builder,
        shouldQueries: List<Query>,
        filters: Map<String, String>,
    ): ObjectBuilder<Query> = query.bool { bool ->
        bool.filter { filter ->
            buildExactKeywordFilter(filter, TYPE_FIELD, Type.Organization.name)
        }
        addEntityGuidGroupingFilter(bool)

        filters
            .filterKeys { it != TYPE_FIELD }
            .forEach { (fieldName, fieldValue) ->
                bool.filter { filter ->
                    buildMultiValueFilterQuery(filter, fieldName, fieldValue)
                }
            }

        shouldQueries.forEach(bool::should)
        bool.minimumShouldMatch("1")
        bool
    }

    private fun buildOrganizationExactNameQuery(
        rawKeyword: String,
        organizationName: String,
    ): Query = Query.of { query ->
        query.bool { bool ->
            organizationExactValues(rawKeyword, organizationName)
                .forEach { exactValue ->
                    bool.should { should ->
                        should.term { term ->
                            term.field(NAME_KEYWORD_FIELD)
                            term.value(exactValue)
                            term.caseInsensitive(true)
                        }
                    }
                }
            bool.minimumShouldMatch("1")
            bool
        }
    }

    private fun buildOrganizationPhraseQuery(
        organizationName: String,
    ): Query = Query.of { query ->
        query.multiMatch { multiMatch ->
            multiMatch.fields(ORGANIZATION_TEXT_FIELDS)
            multiMatch.query(organizationName)
            multiMatch.type(TextQueryType.Phrase)
        }
    }

    private fun buildOrganizationMultiMatchQuery(
        organizationName: String,
        type: TextQueryType,
        operator: Operator,
    ): Query = Query.of { query ->
        query.multiMatch { multiMatch ->
            multiMatch.fields(ORGANIZATION_TEXT_FIELDS)
            multiMatch.query(organizationName)
            multiMatch.type(type)
            multiMatch.operator(operator)
        }
    }

    private fun buildOrganizationWildcardTokenQuery(
        token: String,
    ): Query = Query.of { query ->
        query.bool { bool ->
            ORGANIZATION_KEYWORD_FIELDS
                .map { fieldName -> buildWildcardContainsQuery(fieldName, token) }
                .forEach(bool::should)
            bool.minimumShouldMatch("1")
            bool
        }
    }

    private fun buildCodeSearchRequest(
        type: Type,
        fieldName: String,
        searchQuery: OrganizationSearchQuery,
        pageable: Pageable,
        rankRange: ElasticSearchRankRangeResponse,
    ): RankedSearchRequest {
        val code = textNormalizer.compact(searchQuery.raw)

        return buildTypedSearchRequest(
            type = type,
            fields = listOf(
                FieldSearchConfig(
                    fieldName = fieldName,
                    query = code,
                    matchWeights = OrganizationMatchWeights,
                    fuzzyMatching = false,
                ),
            ),
            filters = searchQuery.filters,
            pageable = pageable,
            rankRange = rankRange,
            minimumShouldMatch = "1",
        )
    }

    private fun buildIndividualSearchRequest(
        searchQuery: OrganizationSearchQuery,
        pageable: Pageable,
        rankRange: ElasticSearchRankRangeResponse,
    ): RankedSearchRequest {
        val identificationNumber = searchQuery.raw.identificationNumberSearchValue()
        val nameParts = searchQuery.nameSearchTokens(identificationNumber)
        val nameTokenQueries = nameParts.map(::buildIndividualNameTokenQuery)

        return buildTypedSearchRequest(
            type = Type.Individual,
            fields = buildList {
                identificationNumber?.let {
                    add(
                        FieldSearchConfig(
                            IDENTIFICATION_NUMBER_FIELD,
                            it,
                            IdentificationNumberMatchWeights,
                            fuzzyMatching = false,
                            multiValueMatching = true,
                            queryVariants = searchQuery.raw.searchValueVariants(),
                            scoreScaleContribution = 0.0,
                        )
                    )
                }
            },
            filters = searchQuery.filters,
            pageable = pageable,
            rankRange = rankRange,
            minimumShouldMatch = "1",
            additionalShouldQueries = nameTokenQueries,
            additionalScoreScaleContribution = nameTokenQueries.size * IndividualMatchWeights.maxRawScore.toDouble(),
        )
    }

    private fun buildIndividualNameTokenQuery(token: String): Query {
        val queries = INDIVIDUAL_NAME_FIELDS
            .flatMap { fieldName ->
                buildList {
                    addAll(
                        buildFuzzyMatchQueries(
                            fieldConfig = FieldSearchConfig(fieldName, token, IndividualMatchWeights),
                            queryValue = token,
                        )
                    )

                    if (token.length >= MIN_WILDCARD_VALUE_LENGTH) {
                        addAll(
                            buildConstantScoreContainsQueries(
                                fieldName = fieldName,
                                queryValue = token,
                                boost = IndividualMatchWeights.exactMatch,
                            )
                        )
                    }
                }
            }
            .distinctBy { it.toString() }

        return Query.of { query ->
            query.disMax { disMax ->
                disMax.queries(queries)
            }
        }
    }

    private fun buildTypedSearchRequest(
        type: Type,
        fields: List<FieldSearchConfig>,
        filters: Map<String, String>,
        pageable: Pageable,
        rankRange: ElasticSearchRankRangeResponse,
        minimumShouldMatch: String? = null,
        additionalShouldQueries: List<Query> = emptyList(),
        additionalScoreScaleContribution: Double = 0.0,
    ): RankedSearchRequest {
        val activeFields = fields.filter { it.query.isNotBlank() }
        val scoreScaleMaxRawScore = activeFields.scoreScaleMaxRawScore(additionalScoreScaleContribution)

        return RankedSearchRequest(
            request = SearchRequest.of { request ->
                request.index(properties.elastic.index)
                request.from(pageable.offset.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                request.size(pageable.pageSize)
                request.minScore(rankRange.minRawScore(scoreScaleMaxRawScore))
                request.applySearchPerformanceOptions()
                request.applyEntityGuidGrouping()
                request.source { source ->
                    source.filter { filter ->
                        filter.includes(sourceFields())
                    }
                }
                request.query { query ->
                    buildBoolQuery(query, type, activeFields, filters, minimumShouldMatch, additionalShouldQueries)
                }
            },
            scoreScale = SearchScoreScale(scoreScaleMaxRawScore),
        )
    }

    private fun List<FieldSearchConfig>.scoreScaleMaxRawScore(additionalScoreScaleContribution: Double = 0.0): Double {
        val explicitContribution = sumOf { it.scoreScaleContribution } + additionalScoreScaleContribution
        if (explicitContribution > 0.0) {
            return explicitContribution
        }

        return maxOfOrNull { it.matchWeights.maxRawScore.toDouble() } ?: 0.0
    }

    private fun buildBoolQuery(
        query: Query.Builder,
        type: Type,
        fields: List<FieldSearchConfig>,
        filters: Map<String, String>,
        minimumShouldMatch: String?,
        additionalShouldQueries: List<Query>,
    ): ObjectBuilder<Query> = query.bool { bool ->
        bool.filter { filter ->
            buildExactKeywordFilter(filter, TYPE_FIELD, type.name)
        }
        addEntityGuidGroupingFilter(bool)

        filters
            .filterKeys { it != TYPE_FIELD }
            .forEach { (fieldName, fieldValue) ->
                bool.filter { filter ->
                    buildMultiValueFilterQuery(filter, fieldName, fieldValue)
                }
            }

        fields.forEach { field ->
            addBestFieldMatchClause(bool, field)
        }
        additionalShouldQueries.forEach(bool::should)

        minimumShouldMatch?.let(bool::minimumShouldMatch)
        bool
    }

    private fun addEntityGuidGroupingFilter(
        bool: co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder,
    ) {
        val collapseField = entityGuidCollapseField()

        bool.filter { filter ->
            filter.exists { exists ->
                exists.field(collapseField)
            }
        }
        bool.mustNot { mustNot ->
            mustNot.term { term ->
                term.field(collapseField)
                term.value("")
            }
        }
    }

    private fun addBestFieldMatchClause(
        bool: co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder,
        fieldConfig: FieldSearchConfig,
    ) {
        val queries = fieldConfig.queryVariants
            .flatMap { queryValue ->
                buildList {
                    when {
                        fieldConfig.multiValueMatching -> addAll(
                            buildConstantScoreMultiValueQueries(
                                fieldName = fieldConfig.fieldName,
                                queryValue = queryValue,
                                boost = fieldConfig.matchWeights.exactMatch,
                            )
                        )

                        fieldConfig.fuzzyMatching -> addAll(
                            buildFuzzyMatchQueries(fieldConfig, queryValue)
                        )

                        else -> add(
                            buildConstantScoreMatchQuery(
                                fieldName = fieldConfig.fieldName,
                                queryValue = queryValue,
                                fuzziness = null,
                                boost = fieldConfig.matchWeights.exactMatch
                            )
                        )
                    }

                    if (fieldConfig.containsMatching) {
                        addAll(
                            buildConstantScoreContainsQueries(
                                fieldName = fieldConfig.fieldName,
                                queryValue = queryValue,
                                boost = fieldConfig.matchWeights.exactMatch,
                            )
                        )
                    }
                }
            }
            .distinctBy { it.toString() }

        bool.should { should ->
            should.disMax { disMax ->
                disMax.queries(queries)
            }
        }
    }

    private fun buildMultiValueFilterQuery(
        query: Query.Builder,
        fieldName: String,
        fieldValue: String,
    ): ObjectBuilder<Query> = query.bool { bool ->
        buildExactFilterValueQueries(fieldName, fieldValue)
            .forEach(bool::should)

        bool.minimumShouldMatch("1")
    }

    private fun buildFuzzyMatchQueries(
        fieldConfig: FieldSearchConfig,
        queryValue: String,
    ): List<Query> = buildList {
        add(
            buildConstantScoreMatchQuery(
                fieldName = fieldConfig.fieldName,
                queryValue = queryValue,
                fuzziness = null,
                boost = fieldConfig.matchWeights.exactMatch
            )
        )

        if (queryValue.length >= MIN_FUZZY_ONE_LENGTH) {
            add(
                buildConstantScoreMatchQuery(
                    fieldName = fieldConfig.fieldName,
                    queryValue = queryValue,
                    fuzziness = "1",
                    boost = fieldConfig.matchWeights.fuzzyMatchOneTypo
                )
            )
        }

        if (queryValue.length >= MIN_FUZZY_TWO_LENGTH) {
            add(
                buildConstantScoreMatchQuery(
                    fieldName = fieldConfig.fieldName,
                    queryValue = queryValue,
                    fuzziness = "2",
                    boost = fieldConfig.matchWeights.fuzzyMatchTwoTypos
                )
            )
        }
    }

    private fun buildConstantScoreMultiValueQueries(
        fieldName: String,
        queryValue: String,
        boost: Float,
    ): List<Query> = buildMultiValueQueries(fieldName, queryValue)
        .map { multiValueQuery -> buildConstantScoreQuery(multiValueQuery, boost) }

    private fun buildConstantScoreContainsQueries(
        fieldName: String,
        queryValue: String,
        boost: Float,
    ): List<Query> = listOf(fieldName, "$fieldName.keyword")
        .map { containsFieldName ->
            buildConstantScoreQuery(
                filterQuery = buildWildcardContainsQuery(containsFieldName, queryValue),
                boost = boost,
            )
        }

    private fun buildConstantScoreQuery(
        filterQuery: Query,
        boost: Float,
    ): Query = Query.of { query ->
        query.constantScore { constantScore ->
            constantScore.filter(filterQuery)
            constantScore.boost(boost)
        }
    }

    private fun buildMultiValueQueries(
        fieldName: String,
        rawValue: String,
    ): List<Query> = rawValue.searchValueVariants()
        .flatMap { queryValue ->
            buildList {
                add(buildMatchPhraseQuery(fieldName, queryValue))
                add(buildCaseInsensitiveTermQuery("$fieldName.keyword", queryValue))

                if (queryValue.length >= MIN_WILDCARD_VALUE_LENGTH) {
                    add(buildWildcardContainsQuery("$fieldName.keyword", queryValue))
                }
            }
        }
        .distinctBy { it.toString() }

    private fun buildExactFilterValueQueries(
        fieldName: String,
        rawValue: String,
    ): List<Query> = rawValue.searchValueVariants()
        .flatMap { queryValue ->
            listOf(
                buildMatchPhraseQuery(fieldName, queryValue),
                buildCaseInsensitiveTermQuery("$fieldName.keyword", queryValue),
            )
        }
        .distinctBy { it.toString() }

    private fun buildMatchPhraseQuery(
        fieldName: String,
        queryValue: String,
    ): Query = Query.of { query ->
        query.matchPhrase { matchPhrase ->
            matchPhrase.field(fieldName)
            matchPhrase.query(queryValue)
        }
    }

    private fun buildCaseInsensitiveTermQuery(
        fieldName: String,
        queryValue: String,
    ): Query = Query.of { query ->
        query.term { term ->
            term.field(fieldName)
            term.value(queryValue)
            term.caseInsensitive(true)
        }
    }

    private fun buildExactKeywordFilter(
        query: Query.Builder,
        fieldName: String,
        fieldValue: String,
    ): ObjectBuilder<Query> = query.term { term ->
        term.field(fieldName)
        term.value(fieldValue)
        term.caseInsensitive(true)
    }

    private fun buildWildcardContainsQuery(
        fieldName: String,
        queryValue: String,
    ): Query = Query.of { query ->
        query.wildcard { wildcard ->
            wildcard.field(fieldName)
            wildcard.value(queryValue.toContainsWildcard())
            wildcard.caseInsensitive(true)
        }
    }

    private fun String.searchValueVariants(): List<String> = listOf(
        trim(),
        textNormalizer.normalize(this),
        textNormalizer.compact(this),
    )
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()

    private fun OrganizationSearchQuery.nameSearchTokens(identificationNumber: String?): List<String> {
        if (identificationNumber == null) {
            return tokens.ifEmpty {
                listOf(textNormalizer.compact(raw)).filter { it.length >= MIN_TERM_LENGTH }
            }
        }

        return tokens.filter { token -> token.any(Char::isLetter) && token.none(Char::isDigit) }
    }

    private fun String.toSearchTokens(): List<String> = trim()
        .split(WHITESPACE_REGEX)
        .asSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()
        .toList()

    private fun organizationExactValues(
        rawKeyword: String,
        organizationName: String,
    ): List<String> = listOf(rawKeyword.trim(), organizationName)
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinctBy(String::lowercase)

    private fun String.organizationIdentificationNumberSearchValue(): String? = textNormalizer.compact(this)
        .takeIf { value -> value.any(Char::isDigit) }

    private fun String.identificationNumberSearchValue(): String? = textNormalizer.compact(this)
        .takeIf { value -> value.any { it.isLetter() } && value.any { it.isDigit() } }

    private fun String.isIdentifierDominantSearch(): Boolean {
        val value = textNormalizer.compact(this)
        val digits = value.count(Char::isDigit)
        val letters = value.count(Char::isLetter)

        return digits >= MIN_IDENTIFIER_DIGITS && letters <= MAX_IDENTIFIER_PREFIX_LETTERS
    }

    private fun String.toContainsWildcard(): String = buildString {
        append('*')
        this@toContainsWildcard.forEach { char ->
            if (char == '*' || char == '?' || char == '\\') {
                append('\\')
            }
            append(char)
        }
        append('*')
    }

    private fun buildConstantScoreMatchQuery(
        fieldName: String,
        queryValue: String,
        fuzziness: String?,
        boost: Float,
    ): Query = Query.of { query ->
        query.constantScore { constantScore ->
            constantScore.filter { filter ->
                filter.match { match ->
                    match.field(fieldName)
                    match.query(queryValue)
                    fuzziness?.let(match::fuzziness)
                    match
                }
            }
            constantScore.boost(boost)
        }
    }

    private fun SearchRequest.Builder.applySearchPerformanceOptions(): SearchRequest.Builder {
        trackTotalHits { trackTotalHits ->
            trackTotalHits.count(properties.elastic.trackTotalHitsUpTo.coerceAtLeast(0))
        }
        timeout(properties.elastic.searchTimeout.takeIf(String::isNotBlank))
        requestCache(properties.elastic.requestCache)

        return this
    }

    private fun SearchRequest.Builder.applyEntityGuidGrouping(): SearchRequest.Builder {
        val collapseField = entityGuidCollapseField()

        collapse { collapse ->
            collapse.field(collapseField)
        }
        sort { sort ->
            sort.score { score ->
                score.order(SortOrder.Desc)
            }
        }
        aggregations(ENTITY_GUID_GROUP_COUNT_AGGREGATION) { aggregation ->
            aggregation.cardinality { cardinality ->
                cardinality.field(collapseField)
                cardinality.precisionThreshold(MAX_CARDINALITY_PRECISION_THRESHOLD)
            }
        }

        return this
    }

    private fun SearchResponse<Map<*, *>>.toSearchResponse(
        pageable: Pageable,
        scoreScale: SearchScoreScale,
        rankRange: ElasticSearchRankRangeResponse,
    ): ElasticsearchSearchResponse {
        val hitsMetadata = hits()
        val hits = hitsMetadata.hits()
        val items = hits.map { hit ->
            val source: Map<*, *> = hit.source() ?: emptyMap<String, Any?>()
            val consolidatedSanctionEntityGuId = source.stringValue(entityGuidSourceField())

            ElasticsearchSearchHit(
                id = hit.id() ?: source.stringValue(ID_FIELD).orEmpty(),
                score = scoreScale.normalize(hit.score()),
                name = preferredName(source),
                fullName = source.stringValue(FULL_NAME_FIELD),
                firstname = source.stringValue(FIRSTNAME_FIELD),
                lastname = source.stringValue(LASTNAME_FIELD),
                identificationNumber = source.stringValue(IDENTIFICATION_NUMBER_FIELD),
                region = source.stringValue(REGION_FIELD),
                birth = source.stringValue(BIRTH_FIELD),
                entityGuid = consolidatedSanctionEntityGuId,
                entityAdverseMedia = source.booleanValue(ENTITY_ADVERSE_MEDIA_FIELD),
                extLookUpId = source.stringValue(EXT_LOOK_UP_ID_FIELD),
                type = source.stringValue(TYPE_FIELD),
                complianceType = source.stringValue(COMPLIANCE_TYPE_FIELD),
                dataType = source.stringValue(DATA_TYPE_FIELD),
            )
        }.filter { hit -> rankRange.contains(hit.score) }
        val totalElements = entityGuidGroupCount() ?: hitsMetadata.total()?.value() ?: items.size.toLong()

        return ElasticsearchSearchResponse(
            took = took().coerceAtLeast(0L),
            total = totalElements.coerceAtLeast(0L),
            maxScore = items.maxOfOrNull(ElasticsearchSearchHit::score) ?: 0.0,
            page = pageable.pageNumber,
            size = pageable.pageSize,
            list = items,
        )
    }

    private fun preferredName(source: Map<*, *>): String? = source.stringValue(NAME_FIELD)
        ?: source.stringValue(FULL_NAME_FIELD)

    private fun sourceFields(): List<String> = SOURCE_FIELDS + entityGuidSourceField()

    private fun entityGuidSourceField(): String = properties.elastic.entityGuidField
        .trim()
        .takeIf(String::isNotBlank)
        ?: ENTITY_GUID_FIELD

    private fun entityGuidCollapseField(): String = "${entityGuidSourceField()}.keyword"

    private fun SearchResponse<Map<*, *>>.entityGuidGroupCount(): Long? = aggregations()
        .get(ENTITY_GUID_GROUP_COUNT_AGGREGATION)
        ?.takeIf { aggregate -> aggregate.isCardinality }
        ?.cardinality()
        ?.value()

    private fun Map<*, *>.stringValue(key: String): String? = this[key]
        ?.toString()
        ?.takeIf { it.isNotBlank() }

    private fun Map<*, *>.booleanValue(key: String): Boolean = when (val value = this[key]) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull() ?: false
        is Number -> value.toInt() != 0
        else -> false
    }

    private fun emptyResponse(pageable: Pageable): ElasticsearchSearchResponse = ElasticsearchSearchResponse(
        took = 0,
        total = 0,
        maxScore = 0.0,
        page = pageable.pageNumber,
        size = pageable.pageSize,
        list = emptyList(),
    )

    private fun String.maskForLogs(): String = when {
        isBlank() -> "***"
        length <= 2 -> "${first()}***"
        else -> "${take(2)}***"
    }

    private fun ElasticSearchRankRangeResponse.contains(score: Double): Boolean =
        score >= minRange && score <= maxRange

    private fun ElasticSearchRankRangeResponse.minRawScore(maxRawScore: Double): Double {
        if (maxRawScore <= 0.0 || minRange <= 0.0) {
            return 0.0
        }

        return ((minRange / MAX_NORMALIZED_SCORE) * maxRawScore).coerceAtLeast(0.0)
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private const val ID_FIELD = "id"
        private const val FULL_NAME_FIELD = "fullName"
        private const val NAME_FIELD = "name"
        private const val FULL_NAME_KEYWORD_FIELD = "fullName.keyword"
        private const val NAME_KEYWORD_FIELD = "name.keyword"
        private const val FIRSTNAME_FIELD = "firstname"
        private const val LASTNAME_FIELD = "lastname"
        private const val IDENTIFICATION_NUMBER_FIELD = "identificationNumber"
        private const val REGION_FIELD = "region"
        private const val BIRTH_FIELD = "birth"
        private const val SWIFT_FIELD = "swift"
        private const val BIG_FIELD = "big"
        private const val ENTITY_GUID_FIELD = "consolidatedSanctionEntityGuId"
        private const val ENTITY_ADVERSE_MEDIA_FIELD = "entityAdverseMedia"
        private const val EXT_LOOK_UP_ID_FIELD = "extLookUpId"
        private const val TYPE_FIELD = "type"
        private const val COMPLIANCE_TYPE_FIELD = "complianceType"
        private const val DATA_TYPE_FIELD = "dataType"
        private const val MAX_NORMALIZED_SCORE = 100.0
        private const val MIN_TERM_LENGTH = 3
        private const val MIN_IDENTIFIER_DIGITS = 4
        private const val MAX_IDENTIFIER_PREFIX_LETTERS = 3
        private const val MIN_WILDCARD_TOKEN_LENGTH = 4
        private const val MIN_WILDCARD_VALUE_LENGTH = 4
        private const val MIN_FUZZY_ONE_LENGTH = 4
        private const val MIN_FUZZY_TWO_LENGTH = 7
        private const val ORGANIZATION_NAME_SCORE_SCALE_MAX_RAW_SCORE = 100.0
        private const val ENTITY_GUID_GROUP_COUNT_AGGREGATION = "entity_guid_group_count"
        private const val MAX_CARDINALITY_PRECISION_THRESHOLD = 40_000
        private val DEFAULT_ORG_TYPE = OrgType.J

        private val WHITESPACE_REGEX = Regex("\\s+")
        private val INDIVIDUAL_NAME_FIELDS = listOf(FIRSTNAME_FIELD, LASTNAME_FIELD, NAME_FIELD, FULL_NAME_FIELD)
        private val ORGANIZATION_TEXT_FIELDS = listOf(NAME_FIELD, FULL_NAME_FIELD)
        private val ORGANIZATION_KEYWORD_FIELDS = listOf(NAME_KEYWORD_FIELD, FULL_NAME_KEYWORD_FIELD)

        private val SOURCE_FIELDS = listOf(
            ID_FIELD,
            NAME_FIELD,
            FULL_NAME_FIELD,
            FIRSTNAME_FIELD,
            LASTNAME_FIELD,
            IDENTIFICATION_NUMBER_FIELD,
            REGION_FIELD,
            BIRTH_FIELD,
            SWIFT_FIELD,
            BIG_FIELD,
            ENTITY_ADVERSE_MEDIA_FIELD,
            EXT_LOOK_UP_ID_FIELD,
            TYPE_FIELD,
            COMPLIANCE_TYPE_FIELD,
            DATA_TYPE_FIELD,
        )
    }

    private data class FieldSearchConfig(
        val fieldName: String,
        val query: String,
        val matchWeights: FieldMatchWeights,
        val fuzzyMatching: Boolean = true,
        val multiValueMatching: Boolean = false,
        val containsMatching: Boolean = false,
        val queryVariants: List<String> = listOf(query),
        val scoreScaleContribution: Double = matchWeights.maxRawScore.toDouble(),
    )

    private data class RankedSearchRequest(
        val request: SearchRequest,
        val scoreScale: SearchScoreScale,
    )

    private open class FieldMatchWeights(
        val exactMatch: Float,
        val fuzzyMatchOneTypo: Float,
        val fuzzyMatchTwoTypos: Float,
    ) {
        init {
            require((exactMatch >= fuzzyMatchOneTypo) && (fuzzyMatchOneTypo >= fuzzyMatchTwoTypos)) {
                "Field match weights must decrease as matching becomes less precise."
            }
        }

        val maxRawScore: Float
            get() = exactMatch
    }

    private object IndividualMatchWeights : FieldMatchWeights(
        exactMatch = 100f,
        fuzzyMatchOneTypo = 70f,
        fuzzyMatchTwoTypos = 40f,
    )

    private object OrganizationMatchWeights : FieldMatchWeights(
        exactMatch = 100f,
        fuzzyMatchOneTypo = 70f,
        fuzzyMatchTwoTypos = 40f,
    )

    private object IdentificationNumberMatchWeights : FieldMatchWeights(
        exactMatch = 200f,
        fuzzyMatchOneTypo = 0f,
        fuzzyMatchTwoTypos = 0f,
    )
}
