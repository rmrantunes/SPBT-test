package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.FgaRelationshipDef

// For OpenFGA implementation
interface IFgaProvider {
    fun writeRelationships(relationships: List<FgaRelationshipDef>)

    fun deleteRelationships(relationships: List<FgaRelationshipDef>)

    /** If you want to check many at once use `batchCheckRelationships` */
    fun checkRelationship(relationship: FgaRelationshipDef): Boolean
}
