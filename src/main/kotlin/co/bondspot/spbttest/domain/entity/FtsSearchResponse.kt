package co.bondspot.spbttest.domain.entity

data class FtsSearchResponse(
    val hits: List<Map<String, Any>> = emptyList(),
    val estimatedTotalHits: Int = 0,
    val totalHits: Int? = null,
    val offset: Int = 0,
    val limit: Int = 20,
)