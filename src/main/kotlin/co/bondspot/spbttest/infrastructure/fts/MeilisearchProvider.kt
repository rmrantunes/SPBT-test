package co.bondspot.spbttest.infrastructure.fts

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.entity.FtsSearchResponse
import org.springframework.web.client.RestClient

class MeilisearchProvider : IFullTextSearchProvider {
    private val baseUrl = "http://localhost:7700"
    private val masterKey = "aSampleMasterKey"

    private val restClient =
        RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Authorization", "Bearer $masterKey")
            .build()

    override fun <T> index(collection: String, items: List<T>) {
        restClient
            .put()
            .uri("/indexes/$collection/documents")
            .body(items)
            .retrieve()
            .toBodilessEntity()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> search(collection: String, query: String, ids: List<String>?): List<T> {
        val response =
            restClient
                .post()
                .uri("/indexes/$collection/search")
                .body(mapOf("q" to query))
                .retrieve()
                .body(FtsSearchResponse::class.java)

        return response?.hits as? List<T> ?: emptyList()
    }
}
