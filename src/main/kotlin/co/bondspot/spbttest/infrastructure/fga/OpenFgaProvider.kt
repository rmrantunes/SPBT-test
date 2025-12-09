package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.entity.EntityName
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.ID
import co.bondspot.spbttest.domain.exception.FgaProviderException

class OpenFgaProviderException(message: String) : FgaProviderException(message)

class OpenFgaProvider : IFgaProvider {
    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun writeRelationships(relationships: List<FgaRelTuple>) {
        TODO("Not yet implemented")
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun writeRelationship(relationship: FgaRelTuple) {
        TODO("Not yet implemented")
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun deleteRelationships(relationships: List<FgaRelTuple>) {
        TODO("Not yet implemented")
    }

    /**
     * If you want to check many at once use `batchCheckRelationships`
     *
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun checkRelationship(relationship: FgaRelTuple): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun listObjects(
        actor: Pair<EntityName, ID>,
        relation: String,
        type: EntityName,
    ): List<String> {
        TODO("Not yet implemented")
    }
}
