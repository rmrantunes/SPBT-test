package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.EntityName
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.ID

// For OpenFGA implementation
interface IFgaProvider {
    fun writeRelationships(relationships: List<FgaRelTuple>)

    fun writeRelationship(relationship: FgaRelTuple)

    fun deleteRelationships(relationships: List<FgaRelTuple>)

    /** If you want to check many at once use `batchCheckRelationships` */
    fun checkRelationship(relationship: FgaRelTuple): Boolean

    fun listObjects(actor: Pair<EntityName, ID>, relation: String, type: EntityName): List<String>
}
