package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.contract.EntityName
import co.bondspot.spbttest.domain.contract.ID
import co.bondspot.spbttest.domain.contract.IFgaProvider

class OpenFgaProvider : IFgaProvider {
    override fun writeRelationship(
        actor: Pair<EntityName, ID>,
        relation: String,
        subject: Pair<EntityName, ID>
    ) {
        TODO("Not yet implemented")
    }
}