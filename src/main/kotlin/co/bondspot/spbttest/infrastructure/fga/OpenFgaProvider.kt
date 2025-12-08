package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.entity.EntityName
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.ID

class OpenFgaProvider : IFgaProvider {
    override fun writeRelationships(relationships: List<FgaRelTuple>) {
        TODO("Not yet implemented")
    }

    override fun writeRelationship(relationship: FgaRelTuple) {
        TODO("Not yet implemented")
    }

    override fun deleteRelationships(relationships: List<FgaRelTuple>) {
        TODO("Not yet implemented")
    }

    override fun checkRelationship(relationship: FgaRelTuple): Boolean {
        TODO("Not yet implemented")
    }

    override fun listObjects(
        actor: Pair<EntityName, ID>,
        relation: String,
        type: EntityName
    ): List<String> {
        TODO("Not yet implemented")
    }
}
