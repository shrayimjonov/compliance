package uz.davrbank.compliance

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import uz.davrbank.compliance.backup.AdditionalSegment
import uz.davrbank.compliance.backup.Compliance
import uz.davrbank.compliance.backup.EntityAddress
import uz.davrbank.compliance.backup.EntityAdverseMedia
import uz.davrbank.compliance.backup.EntityAdverseMediaSubcategory
import uz.davrbank.compliance.backup.EntityAlias
import uz.davrbank.compliance.backup.EntityCountryAssociation
import uz.davrbank.compliance.backup.EntityDOB
import uz.davrbank.compliance.backup.EntityEnforcement
import uz.davrbank.compliance.backup.EntityEnforcementSubcategory
import uz.davrbank.compliance.backup.EntityIdentification
import uz.davrbank.compliance.backup.EntityPEP
import uz.davrbank.compliance.backup.EntityPEPSubcategory
import uz.davrbank.compliance.backup.EntityPosition
import uz.davrbank.compliance.backup.EntityRelationship
import uz.davrbank.compliance.backup.EntityRemark
import uz.davrbank.compliance.backup.EntitySOE
import uz.davrbank.compliance.backup.EntitySOEDomain
import uz.davrbank.compliance.backup.EntitySOESubcategory
import uz.davrbank.compliance.backup.EntitySanction
import uz.davrbank.compliance.backup.EntitySourceItem
import uz.davrbank.compliance.backup.Field
import uz.davrbank.compliance.backup.Record
import java.util.Date



@Document(collection = "abs_data_item_logs")
class AbsDataCheckerItem(
    @Indexed var absDataCheckerId: String,
    var compliance: Compliance? = null,
    var entityDOB: MutableList<EntityDOB> = mutableListOf(),
    var entityAddress: MutableList<EntityAddress> = mutableListOf(),
    var entityAlias: MutableList<EntityAlias> = mutableListOf(),
    var entityIdentification: MutableList<EntityIdentification> = mutableListOf(),
    var entityCountryAssociation: MutableList<EntityCountryAssociation> = mutableListOf(),
    var entityPosition: MutableList<EntityPosition> = mutableListOf(),
    var entityRelationship: MutableList<EntityRelationship> = mutableListOf(),
    var entityRemark: MutableList<EntityRemark> = mutableListOf(),
    var entitySourceItem: MutableList<EntitySourceItem> = mutableListOf(),
    var entityPEP: MutableList<EntityPEP> = mutableListOf(),
    var entityPEPSubcategory: MutableList<EntityPEPSubcategory> = mutableListOf(),
    var entityAdverseMedia: MutableList<EntityAdverseMedia> = mutableListOf(),
    var entityAdverseMediaSubcategory: MutableList<EntityAdverseMediaSubcategory> = mutableListOf(),
    var entityEnforcement: MutableList<EntityEnforcement> = mutableListOf(),
    var entityEnforcementSubcategory: MutableList<EntityEnforcementSubcategory> = mutableListOf(),
    var entitySanction: MutableList<EntitySanction> = mutableListOf(),
    var entitySOE: MutableList<EntitySOE> = mutableListOf(),
    var entitySOESubcategory: MutableList<EntitySOESubcategory> = mutableListOf(),
    var entitySOEDomain: MutableList<EntitySOEDomain> = mutableListOf(),
    var additionalSegment: MutableList<AdditionalSegment> = mutableListOf(),
    var record: MutableList<Record> = mutableListOf(),
    var field: MutableList<Field> = mutableListOf(),
    val createdDate: Date = Date(),
    @org.springframework.data.annotation.Id var id: String? = null
)


@CompoundIndexes(
    CompoundIndex(def = "{'createdDate': -1}")
)
@Document
class ApiLog(
    var header: Any? = null,
    var method: String,
    var uri: String,
    val urlParams: Map<String, String>,
    var requestBody: String? = null,
    var responseBody: Any? = null,
    var encoding: String? = null,
    val createdDate: Date = Date(),
    @Id var id: String? = null
)