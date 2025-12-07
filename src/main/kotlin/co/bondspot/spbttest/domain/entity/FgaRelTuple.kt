package co.bondspot.spbttest.domain.entity

typealias EntityName = String

typealias ID = String

data class FgaRelTuple(
    val actor: Pair<EntityName, ID>,
    val relation: String,
    val subject: Pair<EntityName, ID>,
)
