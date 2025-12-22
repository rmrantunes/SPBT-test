package co.bondspot.spbttest.domain.entity

const val FTS_DEFAULT_PRIMARY_KEY = "id"

data class FtsSearchResponse(
    val hits: List<Map<String, Any>> = emptyList(),
    val estimatedTotalHits: Int = 0,
    val totalHits: Int? = null,
    val totalPages: Int? = null,
    val page: Int = 1,
    val hitsPerPage: Int = 20,
) {
    fun hitsIds() = hits.mapNotNull { it[FTS_DEFAULT_PRIMARY_KEY] as? String }
}
