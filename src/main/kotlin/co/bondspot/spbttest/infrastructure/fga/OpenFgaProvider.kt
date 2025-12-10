package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.entity.EntityName
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.ID
import co.bondspot.spbttest.domain.exception.FgaProviderException
import dev.openfga.sdk.api.client.OpenFgaClient
import dev.openfga.sdk.api.client.model.ClientCheckRequest
import dev.openfga.sdk.api.client.model.ClientListObjectsRequest
import dev.openfga.sdk.api.client.model.ClientTupleKey
import dev.openfga.sdk.api.client.model.ClientWriteRequest
import dev.openfga.sdk.api.configuration.ApiToken
import dev.openfga.sdk.api.configuration.ClientConfiguration
import dev.openfga.sdk.api.configuration.Credentials
import dev.openfga.sdk.errors.FgaError

class OpenFgaProviderException(message: String? = null, cause: Throwable? = null) :
    FgaProviderException(message, cause)

class OpenFgaProvider : IFgaProvider {
    private val apiUrl = System.getenv("OPENFGA_API_URL")
    private val storeId = System.getenv("OPENFGA_STORE_ID")
    private val authorizationModelId = System.getenv("OPENFGA_AUTH_MODEL_ID")
    private val apiToken = System.getenv("OPENFGA_API_TOKEN")

    private val config =
        ClientConfiguration()
            .apiUrl(apiUrl)
            .storeId(storeId)
            .authorizationModelId(authorizationModelId)
            .credentials(Credentials(ApiToken(apiToken)))

    private val client = OpenFgaClient(config)

    private fun FgaRelTuple.toClientTupleKey() =
        ClientTupleKey()
            .user("${actor.first}:${actor.second}")
            .relation(relation)
            ._object("${subject.first}:${subject.second}")

    private fun FgaRelTuple.toClientCheckRequest() =
        ClientCheckRequest()
            .user("${actor.first}:${actor.second}")
            .relation(relation)
            ._object("${subject.first}:${subject.second}")

    private fun FgaError.getMessageFromResponse(): String? {
        return Regex("\"message\":\"(\\w.+)\"").find(responseData)?.groups?.get(1)?.value
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    private fun handleFgaError(ex: Exception): Throwable {
        ex.cause.let { cause ->
            if (cause is FgaError) {
                return OpenFgaProviderException(
                    cause.getMessageFromResponse()
                        ?: "OpenFGA request returned status code ${cause.statusCode}",
                    ex.cause,
                )
            }
        }

        return OpenFgaProviderException(
            ex.message ?: "Something very wrong with OpenFgaProvider requests",
            ex.cause,
        )
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun writeRelationships(relationships: List<FgaRelTuple>) {
        try {
            ClientWriteRequest().writes(relationships.map { it.toClientTupleKey() }).let {
                client.write(it).get()
            }
        } catch (ex: Exception) {
            throw handleFgaError(ex)
        }
    }

    /**
     * Abstraction of `.writeRelationships()`
     *
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun writeRelationship(relationship: FgaRelTuple) {
        writeRelationships(listOf(relationship))
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun deleteRelationships(relationships: List<FgaRelTuple>) {
        try {
            client.deleteTuples(relationships.map { it.toClientTupleKey() }).get()
        } catch (ex: Exception) {
            throw handleFgaError(ex)
        }
    }

    /**
     * Abstraction of `.deleteRelationships()`
     *
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun deleteRelationship(relationship: FgaRelTuple) {
        deleteRelationships(listOf(relationship))
    }

    /**
     * If you want to check many at once use `batchCheckRelationships`
     *
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun checkRelationship(relationship: FgaRelTuple): Boolean {
        return try {
            client.check(relationship.toClientCheckRequest()).get().allowed ?: false
        } catch (ex: Exception) {
            throw handleFgaError(ex)
        }
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun listObjects(
        actor: Pair<EntityName, ID>,
        relation: String,
        type: EntityName,
    ): List<Pair<EntityName, ID>> {
        return try {
            ClientListObjectsRequest()
                .user("${actor.first}:${actor.second}")
                .relation(relation)
                .type(type)
                .let {
                    client.listObjects(it).get().objects.map { rel ->
                        rel.split(":").let { (type, id) -> type to id }
                    }
                }
        } catch (ex: Exception) {
            throw handleFgaError(ex)
        }
    }
}
