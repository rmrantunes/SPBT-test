package co.bondspot.spbttest.domain.entity

data class FtsSearchResponse<T>(
    val hits: List<T>,
    val estimatedTotalHits: Int,
    val totalHits: Int? = null,
    val offset: Int = 0,
    val limit: Int = 20,
)