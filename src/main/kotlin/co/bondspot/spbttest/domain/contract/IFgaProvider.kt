package co.bondspot.spbttest.domain.contract

typealias EntityName = String
typealias ID = String

// For OpenFGA implementation
interface IFgaProvider {
    fun writeRelationship(
        actor: Pair<EntityName, ID>,
        relation: String,
        subject: Pair<EntityName, ID>
    )
}