package co.bondspot.spbttest.infrastructure.fts

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.entity.FtsSearchResponse
import co.bondspot.spbttest.domain.exception.FullTextSearchProviderException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClient

class MeilisearchProviderException(message: String, cause: Throwable? = null) :
    FullTextSearchProviderException(message, cause)

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

    override fun index(indexUid: String, items: List<Any>) {
        try {
            restClient
                .put()
                .uri("/indexes/$indexUid/documents")
                .body(items)
                .retrieve()
                .toBodilessEntity()
        } catch (ex: HttpStatusCodeException) {
            val body = ex.getResponseBodyAs(Map::class.java)
            val apiErrorMessage = body?.getOrDefault("message", "Unknown error") as String
            throw MeilisearchProviderException(
                "Meilisearch API responded with error status code ${ex.statusCode.value()}: $apiErrorMessage",
                ex,
            )
        } catch (ex: Exception) {
            throw MeilisearchProviderException(
                ex.message ?: "Something wrong when indexing $indexUid: $items",
                ex,
            )
        }
    }

    override fun search(indexUid: String, query: String, ids: List<String>?): FtsSearchResponse {
        val body = buildMap {
            set("q", query)
            if (ids != null && ids.isNotEmpty()) {
                set("filter", listOf(ids.map { "id = $it" }))
            }
        }

        val response =
            restClient
                .post()
                .uri("/indexes/$indexUid/search")
                .body(body)
                .retrieve()
                .toEntity(FtsSearchResponse::class.java)

        return response.body ?: FtsSearchResponse()
    }

    /** Wipes all documents from a Meilisearch Index. Be careful. */
    fun dangerouslyDeleteAllDocuments(index: String) {
        restClient.delete().uri("/indexes/$index/documents").retrieve().toBodilessEntity()
    }

    fun setFilterableAttributes(index: String, attributes: List<String>) {
        restClient
            .put()
            .uri("/indexes/$index/settings/filterable-attributes")
            .body(attributes)
            .retrieve()
            .toBodilessEntity()
    }
}
