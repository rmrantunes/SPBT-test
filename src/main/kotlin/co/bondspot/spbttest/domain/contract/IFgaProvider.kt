package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.EntityName
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.ID
import co.bondspot.spbttest.domain.exception.FgaProviderException

// For OpenFGA implementation
interface IFgaProvider {
    /** @throws FgaProviderException */
    fun writeRelationships(relationships: List<FgaRelTuple>)

    /**
     * Abstraction of `.writeRelationships()`
     *
     * @throws FgaProviderException
     */
    fun writeRelationship(relationship: FgaRelTuple)

    /** @throws FgaProviderException */
    fun deleteRelationships(relationships: List<FgaRelTuple>)

    /**
     * Abstraction of `.deleteRelationships()`
     *
     * @throws FgaProviderException
     */
    fun deleteRelationship(relationship: FgaRelTuple)

    /**
     * If you want to check many at once use `batchCheckRelationships`
     *
     * @throws FgaProviderException
     */
    fun checkRelationship(relationship: FgaRelTuple): Boolean

    /** @throws FgaProviderException */
    fun listObjects(actor: Pair<EntityName, ID>, relation: String, type: EntityName): List<String>
}
