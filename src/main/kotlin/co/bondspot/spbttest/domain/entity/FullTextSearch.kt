package co.bondspot.spbttest.domain.entity

const val FTS_DEFAULT_PRIMARY_KEY = "id"

data class FtsSearchResponse(
    val hits: List<Map<String, Any>> = emptyList(),
    val estimatedTotalHits: Int = 0,
    val totalHits: Int? = null,
    val offset: Int = 0,
    val limit: Int = 20,
) {
    fun hitsIds() = hits.mapNotNull { it[FTS_DEFAULT_PRIMARY_KEY] as? String }
}
