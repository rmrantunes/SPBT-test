package co.bondspot.spbttest.domain.entity

data class FtsSearchResponse<T>(
    val records: List<T>,
    val totalResults: Int,
)