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

    override fun index(collection: String, items: List<Any>) {
        restClient
            .put()
            .uri("/indexes/$collection/documents")
            .body(items)
            .retrieve()
            .toBodilessEntity()
    }

    override fun search(collection: String, query: String, ids: List<String>?): FtsSearchResponse {
        val response =
            restClient
                .post()
                .uri("/indexes/$collection/search")
                .body(mapOf("q" to query))
                .retrieve()
                .toEntity(FtsSearchResponse::class.java)

        return response.body ?: FtsSearchResponse()
    }
}
