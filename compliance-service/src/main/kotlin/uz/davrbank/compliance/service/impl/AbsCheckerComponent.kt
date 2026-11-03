package uz.davrbank.compliance.service.impl

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.elasticsearch.core.SearchResponse
import co.elastic.clients.util.ObjectBuilder
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uz.davrbank.compliance.config.AbsCheckerProperties
import uz.davrbank.compliance.config.ApplicationProperties
import uz.davrbank.compliance.domain.AbsCheckerRecord
import uz.davrbank.compliance.domain.AbsCheckerRequestPayload
import uz.davrbank.compliance.domain.AbsCheckerStoredHit
import uz.davrbank.compliance.domain.AbsCheckerStoredHits
import uz.davrbank.compliance.domain.AbsCheckerStoredSearchResponse
import uz.davrbank.compliance.dto.request.abs.AbsCheckerRequest
import uz.davrbank.compliance.dto.response.settings.ElasticSearchRankRangeResponse
import uz.davrbank.compliance.enums.AbsHandlerStatus
import uz.davrbank.compliance.enums.AbsRequestType
import uz.davrbank.compliance.exception.BadRequestException
import uz.davrbank.compliance.exception.ServiceUnavailableException
import uz.davrbank.compliance.repository.AbsCheckerRepository
import uz.davrbank.compliance.service.SettingsService
import uz.davrbank.compliance.util.SearchScoreScale
import uz.davrbank.compliance.util.TextNormalizer

@Component
class AbsCheckerRequestValidator {

    fun validateAndBuildPayload(request: AbsCheckerRequest): AbsCheckerRequestPayload {
        val type = request.type ?: throw BadRequestException("Property 'type' must be provided.")
        val operationType = request.operationType ?: throw BadRequestException("Property 'operationType' must be provided.")

        when (type) {
            AbsRequestType.Individual -> validateIndividualRequest(request)
            AbsRequestType.Organization -> validateOrganizationRequest(request)
            else -> throw BadRequestException("Unsupported type: $type")
        }

        return AbsCheckerRequestPayload(
            isResident = request.isResident,
            firstname = request.firstname.trimToNull(),
            lastname = request.lastname.trimToNull(),
            patronymic = request.patronymic.trimToNull(),
            gender = request.gender.trimToNull(),
            birthday = request.birthday,
            nationality = request.nationality.trimToNull(),
            type = type,
            clientInn = request.clientInn.trimToNull(),
            orgName = request.orgName.trimToNull(),
            country = request.country.trimToNull(),
            operationType = operationType,
            swiftCode = request.swiftCode.trimToNull(),
            bankBik = request.bankBik.trimToNull(),
            address = request.address.trimToNull(),
            director = request.director.trimToNull(),
            directorInn = request.directorInn.trimToNull(),
            founder = request.founder.trimToNull(),
            founderInn = request.founderInn.trimToNull(),
            partnerName = request.partnerName.trimToNull(),
            partnerInn = request.partnerInn.trimToNull(),
            orgRegistrationDate = request.orgRegistrationDate,
            participationShare = request.participationShare,
            branch = request.branch.trimToNull(),
            actionType = request.actionType.trimToNull(),
            requestedDate = request.requestedDate,
            comment = request.comment.trimToNull(),
        )
    }

    private fun validateIndividualRequest(request: AbsCheckerRequest) {
        val missingFields = buildList {
            if (request.firstname.isNullOrBlank()) add("firstname")
            if (request.lastname.isNullOrBlank()) add("lastname")
            if (request.patronymic.isNullOrBlank()) add("patronymic")
            if (request.gender.isNullOrBlank()) add("gender")
            if (request.birthday == null) add("birthday")
            if (request.nationality.isNullOrBlank()) add("nationality")
            if (request.clientInn.isNullOrBlank()) add("clientInn")
            if (request.branch.isNullOrBlank()) add("branch")
        }

        if (missingFields.isNotEmpty()) {
            throw BadRequestException(
                "Missing required fields for Individual: ${missingFields.joinToString(", ")}"
            )
        }
    }

    private fun validateOrganizationRequest(request: AbsCheckerRequest) {
        val missingFields = buildList {
            if (request.orgName.isNullOrBlank()) add("orgName")
            if (request.country.isNullOrBlank()) add("country")
            if (request.swiftCode.isNullOrBlank()) add("swiftCode")
            if (request.address.isNullOrBlank()) add("address")
            if (request.director.isNullOrBlank()) add("director")
            if (request.directorInn.isNullOrBlank()) add("directorInn")
            if (request.founder.isNullOrBlank()) add("founder")
            if (request.founderInn.isNullOrBlank()) add("founderInn")
            if (request.orgRegistrationDate == null) add("orgRegistrationDate")
            if (request.participationShare == null) add("participationShare")
            if (request.branch.isNullOrBlank()) add("branch")
            if (request.actionType.isNullOrBlank()) add("actionType")
            if (request.requestedDate == null) add("requestedDate")
        }

        if (missingFields.isNotEmpty()) {
            throw BadRequestException(
                "Missing required fields for Organization: ${missingFields.joinToString(", ")}"
            )
        }
    }

    private fun String?.trimToNull(): String? = this?.trim()?.takeIf(String::isNotBlank)
}

@Component
class AbsCheckerSearchClient(
    private val elasticsearchClient: ElasticsearchClient,
    private val properties: ApplicationProperties,
    private val absCheckerProperties: AbsCheckerProperties,
    private val textNormalizer: TextNormalizer,
    private val settingsService: SettingsService,
) {

    fun search(request: AbsCheckerRequestPayload): AbsCheckerStoredSearchResponse {
        val rankRange = settingsService.getElasticSearchRankRange()
        val rankedSearchRequest = when (request.type) {
            AbsRequestType.Organization -> buildOrganizationSearchRequest(request, rankRange)
            AbsRequestType.Individual -> buildIndividualSearchRequest(request, rankRange)
            else -> throw BadRequestException("Unsupported type: ${request.type}")
        }

        if (rankedSearchRequest == null) {
            return emptyResponse()
        }

        val response = try {
            elasticsearchClient.search(
                rankedSearchRequest.request,
                Map::class.javaObjectType,
            )
        } catch (ex: Exception) {
            logger.error(ex) {
                "ABS checker Elasticsearch search failed for type='${request.type.name}'"
            }

            throw ServiceUnavailableException(
                message = "Elasticsearch search is currently unavailable.",
                code = "ELASTICSEARCH_UNAVAILABLE",
                cause = ex,
            )
        }

        return response.toStoredResponse(rankedSearchRequest.scoreScale, rankRange)
    }

    private fun buildOrganizationSearchRequest(
        request: AbsCheckerRequestPayload,
        rankRange: ElasticSearchRankRangeResponse,
    ): RankedSearchRequest? {
        val organizationName = textNormalizer.compactOrganization(request.orgName.orEmpty())
        if (organizationName.isBlank()) {
            return null
        }

        return buildSearchRequest(
            type = AbsRequestType.Organization,
            fields = listOf(
                FieldSearchConfig(FULL_NAME_FIELD, organizationName, OrganizationMatchWeights),
            ),
            rankRange = rankRange,
        )
    }

    private fun buildIndividualSearchRequest(
        request: AbsCheckerRequestPayload,
        rankRange: ElasticSearchRankRangeResponse,
    ): RankedSearchRequest? {
        val firstname = textNormalizer.compact(request.firstname.orEmpty())
        val lastname = textNormalizer.compact(request.lastname.orEmpty())
        val activeFields = listOf(
            FieldSearchConfig(FIRSTNAME_FIELD, firstname, IndividualMatchWeights),
            FieldSearchConfig(LASTNAME_FIELD, lastname, IndividualMatchWeights),
        ).filter { it.query.isNotBlank() }

        if (activeFields.isEmpty()) {
            return null
        }

        return buildSearchRequest(
            type = AbsRequestType.Individual,
            fields = activeFields,
            rankRange = rankRange,
        )
    }

    private fun buildSearchRequest(
        type: AbsRequestType,
        fields: List<FieldSearchConfig>,
        rankRange: ElasticSearchRankRangeResponse,
    ): RankedSearchRequest {
        val scoreScaleMaxRawScore = fields.scoreScaleMaxRawScore()

        return RankedSearchRequest(
            request = SearchRequest.of { request ->
                request.index(properties.elastic.index)
                request.size(absCheckerProperties.maxStoredHits.coerceAtLeast(1))
                request.minScore(rankRange.minRawScore(scoreScaleMaxRawScore))
                request.trackTotalHits { trackTotalHits -> trackTotalHits.enabled(true) }
                request.source { source ->
                    source.filter { filter ->
                        filter.includes(SOURCE_FIELDS)
                    }
                }
                request.query { query ->
                    buildBoolQuery(query, type, fields)
                }
            },
            scoreScale = SearchScoreScale(scoreScaleMaxRawScore),
        )
    }

    private fun buildBoolQuery(
        query: Query.Builder,
        type: AbsRequestType,
        fields: List<FieldSearchConfig>,
    ): ObjectBuilder<Query> = query.bool { bool ->
        bool.filter { filter ->
            filter.match { match ->
                match.field(TYPE_FIELD)
                match.query(type.name)
            }
        }

        fields.forEach { field ->
            addBestFieldMatchClause(bool, field)
        }

        bool.minimumShouldMatch("1")
    }

    private fun addBestFieldMatchClause(
        bool: co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder,
        fieldConfig: FieldSearchConfig,
    ) {
        val queries = listOf(
            buildConstantScoreMatchQuery(fieldConfig, fuzziness = null, boost = fieldConfig.matchWeights.exactMatch),
            buildConstantScoreMatchQuery(fieldConfig, fuzziness = "1", boost = fieldConfig.matchWeights.fuzzyMatchOneTypo),
            buildConstantScoreMatchQuery(fieldConfig, fuzziness = "2", boost = fieldConfig.matchWeights.fuzzyMatchTwoTypos),
        )

        bool.should { should ->
            should.disMax { disMax ->
                disMax.queries(queries)
            }
        }
    }

    private fun buildConstantScoreMatchQuery(
        fieldConfig: FieldSearchConfig,
        fuzziness: String?,
        boost: Float,
    ): Query = Query.of { query ->
        query.constantScore { constantScore ->
            constantScore.filter { filter ->
                filter.match { match ->
                    match.field(fieldConfig.fieldName)
                    match.query(fieldConfig.query)
                    fuzziness?.let(match::fuzziness)
                    match
                }
            }
            constantScore.boost(boost)
        }
    }

    private fun SearchResponse<Map<*, *>>.toStoredResponse(
        scoreScale: SearchScoreScale,
        rankRange: ElasticSearchRankRangeResponse,
    ): AbsCheckerStoredSearchResponse {
        val hitsMetadata = hits()
        val hits = hitsMetadata.hits()
        val items = hits.map { hit ->
            val source: Map<*, *> = hit.source() ?: emptyMap<String, Any?>()

            AbsCheckerStoredHit(
                id = hit.id() ?: source.stringValue(ID_FIELD).orEmpty(),
                score = scoreScale.normalize(hit.score()),
                name = preferredName(source),
                fullName = source.stringValue(FULL_NAME_FIELD),
                firstname = source.stringValue(FIRSTNAME_FIELD),
                lastname = source.stringValue(LASTNAME_FIELD),
                identificationNumber = source.stringValue(IDENTIFICATION_NUMBER_FIELD),
                region = source.stringValue(REGION_FIELD),
                birth = source.stringValue(BIRTH_FIELD),
                entityAdverseMedia = source.booleanValue(ENTITY_ADVERSE_MEDIA_FIELD),
                extLookUpId = source.stringValue(EXT_LOOK_UP_ID_FIELD),
                type = source.stringValue(TYPE_FIELD),
                complianceType = source.stringValue(COMPLIANCE_TYPE_FIELD),
                dataType = source.stringValue(DATA_TYPE_FIELD),
            )
        }.filter { hit -> rankRange.contains(hit.score) }

        return AbsCheckerStoredSearchResponse(
            took = took().coerceAtLeast(0L),
            hits = AbsCheckerStoredHits(
                total = items.size.toLong(),
                maxScore = items.maxOfOrNull(AbsCheckerStoredHit::score) ?: 0.0,
                hits = items,
            ),
        )
    }

    private fun List<FieldSearchConfig>.scoreScaleMaxRawScore(): Double {
        val explicitContribution = sumOf { it.scoreScaleContribution }
        if (explicitContribution > 0.0) {
            return explicitContribution
        }

        return maxOfOrNull { it.matchWeights.maxRawScore.toDouble() } ?: 0.0
    }

    private fun preferredName(source: Map<*, *>): String? = source.stringValue(NAME_FIELD)
        ?: source.stringValue(FULL_NAME_FIELD)

    private fun Map<*, *>.stringValue(key: String): String? = this[key]
        ?.toString()
        ?.takeIf(String::isNotBlank)

    private fun Map<*, *>.booleanValue(key: String): Boolean = when (val value = this[key]) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull() ?: false
        is Number -> value.toInt() != 0
        else -> false
    }

    private fun emptyResponse(): AbsCheckerStoredSearchResponse = AbsCheckerStoredSearchResponse(
        took = 0,
        hits = AbsCheckerStoredHits(
            total = 0,
            maxScore = 0.0,
            hits = emptyList(),
        ),
    )

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
        private const val FIRSTNAME_FIELD = "firstname"
        private const val LASTNAME_FIELD = "lastname"
        private const val IDENTIFICATION_NUMBER_FIELD = "identificationNumber"
        private const val REGION_FIELD = "region"
        private const val BIRTH_FIELD = "birth"
        private const val ENTITY_ADVERSE_MEDIA_FIELD = "entityAdverseMedia"
        private const val EXT_LOOK_UP_ID_FIELD = "extLookUpId"
        private const val TYPE_FIELD = "type"
        private const val COMPLIANCE_TYPE_FIELD = "complianceType"
        private const val DATA_TYPE_FIELD = "dataType"
        private const val MAX_NORMALIZED_SCORE = 100.0

        private val SOURCE_FIELDS = listOf(
            ID_FIELD,
            NAME_FIELD,
            FULL_NAME_FIELD,
            FIRSTNAME_FIELD,
            LASTNAME_FIELD,
            IDENTIFICATION_NUMBER_FIELD,
            REGION_FIELD,
            BIRTH_FIELD,
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
}

@Service
class AbsCheckerBackgroundProcessor(
    private val repository: AbsCheckerRepository,
    private val absCheckerRecordProcessor: AbsCheckerRecordProcessor,
    private val properties: AbsCheckerProperties,
) {

    @Scheduled(fixedDelayString = "\${application.abs-checker.poll-delay-ms:300000}")
    fun processRemainingRecords() {
        var processedCount = 0

        while (true) {
            val records = repository.findAllByStatusInOrderByCreatedDateAsc(
                statuses = RETRYABLE_STATUSES,
                pageable = PageRequest.of(0, properties.batchSize.coerceAtLeast(1)),
            ).content

            if (records.isEmpty()) {
                if (processedCount > 0) {
                    logger.info { "Processed $processedCount retryable ABS checker records" }
                }
                return
            }

            records.forEach { record ->
                absCheckerRecordProcessor.processRecord(requireNotNull(record.id))
            }
            processedCount += records.size

            if (records.size < properties.batchSize.coerceAtLeast(1)) {
                logger.info { "Processed $processedCount retryable ABS checker records" }
                return
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private val RETRYABLE_STATUSES = listOf(
            AbsHandlerStatus.CREATED,
            AbsHandlerStatus.FAILED,
        )
    }
}

@Service
class AbsCheckerAsyncProcessor(
    private val absCheckerRecordProcessor: AbsCheckerRecordProcessor,
) {

    @Async("absCheckerExecutor")
    fun processRecord(recordId: String) {
        absCheckerRecordProcessor.processRecord(recordId)
    }
}

@Service
class AbsCheckerRecordProcessor(
    private val repository: AbsCheckerRepository,
    private val absCheckerSearchClient: AbsCheckerSearchClient,
) {

    fun processRecord(recordId: String) {
        val record = repository.findByIdOrNull(recordId)
            ?: return

        if (record.status == AbsHandlerStatus.FINISHED) {
            return
        }

        repository.save(record.process())
        logger.info("-----------Check account record saved to MongoDB-----------")
    }

    private fun AbsCheckerRecord.process(): AbsCheckerRecord =
        try {
            apply {
                response = absCheckerSearchClient.search(request)
                status = AbsHandlerStatus.FINISHED
                comment = null
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to process ABS checker record id='$id'" }

            apply {
                status = AbsHandlerStatus.FAILED
                comment = "Processing failed: ${ex.message}"
            }
        }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
