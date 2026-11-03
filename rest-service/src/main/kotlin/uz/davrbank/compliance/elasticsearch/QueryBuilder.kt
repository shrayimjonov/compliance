package uz.davrbank.compliance.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.search.Hit
import co.elastic.clients.util.ObjectBuilder
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import uz.davrbank.compliance.utils.convertCyrillicToLatin


@Component
class ComplianceSearchQueryBuilder(
    private val elasticsearchClient: ElasticsearchClient
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val INDEX_NAME = "my_compliance_entity"
        private const val INDEX_NAME_V3 = "compliance_data_v3"
        private const val MIN_SCORE = 40.0
        private const val BULK_SEARCH_SIZE = 10_000
    }


    fun search(firstname: String, lastname: String, target: SearchTarget, pageable: Pageable): Page<ESHits> {
        val targetListField = mutableListOf<FieldSearchConfig>()
        if (target == SearchTarget.INDIVIDUAL) {
            targetListField.add(
                FieldSearchConfig("firstname.keyword", firstname, IndividualBoostScores)
            )
            targetListField.add(
                FieldSearchConfig("lastname.keyword", lastname, IndividualBoostScores)
            )
        } else {
            targetListField.add(
                FieldSearchConfig("lastname.keyword", lastname, OrganizationBoostScores)
            )
        }
        val searchRequest = buildSearchRequest(
            targetType = target,
            fields = targetListField,
            from = pageable.pageNumber,
            size = pageable.pageSize,
            minimumShouldMatch = "1",
            isGlobalSearch = true
        )

         return executeSearch(searchRequest, pageable)
    }


    fun searchIndividuals(
        firstname: String,
        lastname: String,
        pageable: Pageable
    ): Page<ESHits> {
        logger.debug {
            "Searching for individuals: firstname='${firstname.take(3)}***', lastname='${lastname.take(3)}***', " +
                    "page=${pageable.pageNumber}, size=${pageable.pageSize}"
        }

        val searchRequest = buildSearchRequest(
            targetType = SearchTarget.INDIVIDUAL,
            fields = listOf(
                FieldSearchConfig("firstname", firstname, IndividualBoostScores),
                FieldSearchConfig("lastname", lastname, IndividualBoostScores)
            ),
            from = pageable.offset.toInt(),
            size = pageable.pageSize,
            useScriptScore = true
        )

        return executeSearch(searchRequest, pageable)
    }


    fun searchOrganizations(
        organizationName: String,
        pageable: Pageable
    ): Page<ESHits> {
        val filteredSearchName = convertCyrillicToLatin(organizationName)
        logger.info {
            "Searching for organizations: name='${organizationName.take(5)}***', " +
                    "page=${pageable.pageNumber}, size=${pageable.pageSize}"
        }

        val searchRequest = buildSearchRequest(
            targetType = SearchTarget.ORGANIZATION,
            fields = listOf(
                FieldSearchConfig("fullName", filteredSearchName, OrganizationBoostScores)
            ),
            from = pageable.offset.toInt(),
            size = pageable.pageSize,
            minimumShouldMatch = "1"
        )

        return executeSearch(searchRequest, pageable)
    }

    fun searchIndividualsInternal(firstname: String, lastname: String): ESHits {
        logger.debug {
            "Bulk search for individuals: firstname='${firstname.take(3)}***', lastname='${lastname.take(3)}***', " +
                    "maxSize=$BULK_SEARCH_SIZE"
        }

        val searchRequest = buildSearchRequest(
            targetType = SearchTarget.INDIVIDUAL,
            fields = listOf(
                FieldSearchConfig("firstname", firstname, IndividualBoostScores),
                FieldSearchConfig("lastname", lastname, IndividualBoostScores)
            ),
            size = BULK_SEARCH_SIZE,
            useScriptScore = true
        )

        return executeInternalSearch(searchRequest)
    }


    fun searchOrganizationsInternal(organizationName: String): ESHits {
        logger.debug {
            "Bulk search for organizations: name='${organizationName.take(5)}***', maxSize=$BULK_SEARCH_SIZE"
        }

        val searchRequest = buildSearchRequest(
            targetType = SearchTarget.ORGANIZATION,
            fields = listOf(
                FieldSearchConfig("fullName", organizationName, OrganizationBoostScores)
            ),
            size = BULK_SEARCH_SIZE,
            minimumShouldMatch = "1"
        )

        return executeInternalSearch(searchRequest)
    }


    private fun buildSearchRequest(
        targetType: SearchTarget,
        fields: List<FieldSearchConfig>,
        from: Int = 0,
        size: Int,
        useScriptScore: Boolean = false,
        isGlobalSearch: Boolean = false,
        minimumShouldMatch: String? = null
    ): SearchRequest {
        logger.trace {
            "Building search request: target=$targetType, fields=${fields.size}, from=$from, size=$size, " +
                    "scriptScore=$useScriptScore, minShouldMatch=$minimumShouldMatch, minScore=$MIN_SCORE"
        }

        return SearchRequest.of { requestBuilder ->
            requestBuilder.index(INDEX_NAME_V3)
            requestBuilder.from(from)
            requestBuilder.size(size)

            if (isGlobalSearch) {
                requestBuilder.query { queryBuilder ->
                    queryBuilder.bool { bool ->
                        bool.filter { filter ->
                            filter.match { match ->
                                match.field("type")
                                match.query(targetType.field)
                            }
                        }

                        fields.forEach { field ->
                            bool.must {
                                it.wildcard { w ->
                                    w.field(field.fieldName)
                                    w.value(field.query)
                                }
                            }
                        }
                        bool
                    }
                }

                return@of requestBuilder
            }


            requestBuilder.minScore(MIN_SCORE)

            requestBuilder.query { queryBuilder ->
                if (useScriptScore) {
                    logger.trace { "Using script score query with Painless script for score rounding" }
                    queryBuilder.scriptScore { scriptScoreBuilder ->
                        scriptScoreBuilder.query { innerQuery ->
                            buildBoolQuery(innerQuery, targetType, fields, minimumShouldMatch)
                        }
                        scriptScoreBuilder.script { script ->
                            script.inline { inline ->
                                inline.lang("painless")
                                inline.source("Math.round(_score)")
                            }
                        }
                    }
                } else {
                    logger.trace { "Using standard bool query without script scoring" }
                    buildBoolQuery(queryBuilder, targetType, fields, minimumShouldMatch)
                }
            }
        }
    }


    private fun buildBoolQuery(
        queryBuilder: Query.Builder,
        targetType: SearchTarget,
        fields: List<FieldSearchConfig>,
        minimumShouldMatch: String?
    ): ObjectBuilder<Query> {
        logger.trace {
            val fieldNames = fields.joinToString { it.fieldName }
            "Building bool query for $targetType with fields: [$fieldNames]"
        }

        return queryBuilder.bool { bool ->

            if (targetType == SearchTarget.INDIVIDUAL) {
                logger.trace { "Using MUST clause for INDIVIDUAL type matching (affects scoring)" }
                bool.must { must ->
                    must.match { match ->
                        match.field("type")
                        match.query(targetType.field)
                    }
                }
            } else {
                logger.trace { "Using FILTER clause for ${targetType.name} type matching (no scoring impact)" }
                bool.filter { filter ->
                    filter.match { match ->
                        match.field("type")
                        match.query(targetType.field)
                    }
                }
            }


            fields.forEach { fieldConfig ->
                addFuzzyMatching(bool, fieldConfig)
            }


            minimumShouldMatch?.let {
                logger.trace { "Applied minimumShouldMatch: $it" }
                bool.minimumShouldMatch(it)
            }


            bool
        }
    }


    private fun addFuzzyMatching(
        boolBuilder: co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder,
        fieldConfig: FieldSearchConfig
    ) {
        logger.trace {
            "Adding fuzzy matching for field '${fieldConfig.fieldName}': " +
                    "exact boost=${fieldConfig.boostScores.exact}, " +
                    "fuzziness1 boost=${fieldConfig.boostScores.fuzziness1}, " +
                    "fuzziness2 boost=${fieldConfig.boostScores.fuzziness2}"
        }

        boolBuilder.should { should ->
            should.constantScore { constantScore ->
                constantScore.filter { filter ->
                    filter.match { match ->
                        match.field(fieldConfig.fieldName)
                        match.query(fieldConfig.query)
                    }
                }
                constantScore.boost(fieldConfig.boostScores.exact)
            }
        }

        // Fuzziness 1 (1 character edit)
        boolBuilder.should { should ->
            should.constantScore { constantScore ->
                constantScore.filter { filter ->
                    filter.match { match ->
                        match.field(fieldConfig.fieldName)
                        match.query(fieldConfig.query)
                        match.fuzziness("1")
                    }
                }
                constantScore.boost(fieldConfig.boostScores.fuzziness1)
            }
        }

        // Fuzziness 2 (2 character edits)
        boolBuilder.should { should ->
            should.constantScore { constantScore ->
                constantScore.filter { filter ->
                    filter.match { match ->
                        match.field(fieldConfig.fieldName)
                        match.query(fieldConfig.query)
                        match.fuzziness("2")
                    }
                }
                constantScore.boost(fieldConfig.boostScores.fuzziness2)
            }
        }
    }


    private fun executeSearch(searchRequest: SearchRequest, pageable: Pageable): Page<ESHits> {
        val startTime = System.currentTimeMillis()
        logger.debug { "Executing Elasticsearch query on index '$INDEX_NAME'..." }

        val response = elasticsearchClient.search(searchRequest, Map::class.javaObjectType)
        val esHits = response.toESHits()
        val executionTime = System.currentTimeMillis() - startTime

        logger.info {
            "Search completed: found ${esHits.hits.total} total hits, " +
                    "returned ${esHits.hits.hits.size} results, " +
                    "ES took ${esHits.took}ms, total time ${executionTime}ms, " +
                    "page ${pageable.pageNumber}, maxScore=${esHits.hits.maxScore}"
        }

        if (esHits.hits.hits.isEmpty()) {
            logger.debug { "No results matched the search criteria (minScore=$MIN_SCORE)" }
        }

        return PageImpl(listOf(esHits), pageable, esHits.hits.total)
    }


    private fun executeInternalSearch(searchRequest: SearchRequest): ESHits {
        val startTime = System.currentTimeMillis()
        logger.debug { "Executing internal bulk search on index '$INDEX_NAME_V3'..." }

        val response = elasticsearchClient.search(searchRequest, Map::class.javaObjectType)
        val esHits = response.toESHits()
        val executionTime = System.currentTimeMillis() - startTime

        logger.info {
            "Bulk search completed: found ${esHits.hits.total} total hits, " +
                    "returned ${esHits.hits.hits.size} results, " +
                    "ES took ${esHits.took}ms, total time ${executionTime}ms, " +
                    "maxScore=${esHits.hits.maxScore}"
        }

        if (esHits.hits.total > BULK_SEARCH_SIZE) {
            logger.warn {
                "Bulk search result count (${esHits.hits.total}) exceeds bulk size ($BULK_SEARCH_SIZE). " +
                        "Some results were truncated."
            }
        }

        if (esHits.hits.hits.isEmpty()) {
            logger.debug { "No results matched the bulk search criteria (minScore=$MIN_SCORE)" }
        }

        return esHits
    }
}


data class FieldSearchConfig(
    val fieldName: String,
    val query: String,
    val boostScores: BoostScores
)


open class BoostScores(
    val exact: Float,
    val fuzziness1: Float,
    val fuzziness2: Float
)


object IndividualBoostScores : BoostScores(
    exact = 5f,
    fuzziness1 = 5f,
    fuzziness2 = 40f
)

object OrganizationBoostScores : BoostScores(
    exact = 50f,
    fuzziness1 = 30f,
    fuzziness2 = 40f
)

