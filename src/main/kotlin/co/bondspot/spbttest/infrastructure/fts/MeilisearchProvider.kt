package co.bondspot.spbttest.infrastructure.fts

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.entity.FtsSearchResponse
import co.bondspot.spbttest.domain.exception.FullTextSearchProviderException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClient

class MeilisearchProviderException(
    message: String,
    cause: Throwable? = null,
    override val contextParams: Map<String, Any?> = emptyMap(),
) : FullTextSearchProviderException(message, cause)

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

    private fun handleException(ex: Exception, contextParams: Map<String, Any?>): Throwable {
        return if (ex is HttpStatusCodeException) {
            val body = ex.getResponseBodyAs(Map::class.java)
            val apiErrorMessage = body?.getOrDefault("message", "Unknown error") as String
            MeilisearchProviderException(
                "Meilisearch API responded with error status code ${ex.statusCode.value()}: $apiErrorMessage",
                ex,
                contextParams = contextParams,
            )
        } else {
            val message =
                listOf(
                        "Something wrong with Meilisearch API call.",
                        ex.message,
                        "See context params for details.",
                    )
                    .filterNotNull()
                    .joinToString(" ")
            MeilisearchProviderException(message, ex, contextParams = contextParams)
        }
    }

    override fun index(indexUid: String, items: List<Any>) {
        val uri = "/indexes/$indexUid/documents"
        try {
            restClient.put().uri(uri).body(items).retrieve().toBodilessEntity()
        } catch (ex: Exception) {
            throw handleException(
                ex,
                mapOf("baseUrl" to baseUrl, "uri" to uri, "indexUid" to indexUid, "items" to items),
            )
        }
    }

    override fun search(indexUid: String, query: String, ids: List<String>?): FtsSearchResponse {
        val uri = "/indexes/$indexUid/search"
        try {
            val body = buildMap {
                set("q", query)
                if (ids != null && ids.isNotEmpty()) {
                    set("filter", listOf(ids.map { "id = $it" }))
                }
            }

            val response =
                restClient
                    .post()
                    .uri(uri)
                    .body(body)
                    .retrieve()
                    .toEntity(FtsSearchResponse::class.java)

            return response.body ?: FtsSearchResponse()
        } catch (ex: Exception) {
            throw handleException(
                ex,
                mapOf(
                    "baseUrl" to baseUrl,
                    "uri" to uri,
                    "indexUid" to indexUid,
                    "query" to query,
                    "ids" to ids,
                ),
            )
        }
    }

    override fun delete(indexUid: String, id: String) {
        val uri = "/indexes/$indexUid/documents/$id"
        try {
            require(id.isNotEmpty()) { "Please inform a non-empty 'id' argument." }

            restClient.delete().uri(uri).retrieve().toBodilessEntity()
        } catch (ex: Exception) {
            throw handleException(
                ex,
                mapOf("baseUrl" to baseUrl, "uri" to uri, "indexUid" to indexUid, "id" to id),
            )
        }
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
