package co.bondspot.spbttest.infrastructure.fga

import co.bondspot.spbttest.domain.contract.IFgaProvider
import co.bondspot.spbttest.domain.entity.EntityName
import co.bondspot.spbttest.domain.entity.FgaRelTuple
import co.bondspot.spbttest.domain.entity.ID
import co.bondspot.spbttest.domain.exception.FgaProviderException
import dev.openfga.sdk.api.client.OpenFgaClient
import dev.openfga.sdk.api.client.model.ClientTupleKey
import dev.openfga.sdk.api.client.model.ClientWriteRequest
import dev.openfga.sdk.api.configuration.ApiToken
import dev.openfga.sdk.api.configuration.ClientConfiguration
import dev.openfga.sdk.api.configuration.Credentials
import dev.openfga.sdk.errors.FgaError

class OpenFgaProviderException(message: String) : FgaProviderException(message)

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

    private fun FgaError.getMessageFromResponse(): String? {
        return Regex("\"message\":\"(\\w.+)\"").find(responseData)?.groups?.get(1)?.value
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    private fun handleFgaError(ex: Exception) {
        ex.cause.let { cause ->
            if (cause is FgaError) {
                throw OpenFgaProviderException(
                    cause.getMessageFromResponse()
                        ?: "OpenFGA request returned status code ${cause.statusCode}"
                )
            }
        }
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
            handleFgaError(ex)

            throw OpenFgaProviderException(
                ex.message ?: "Something very wrong with OpenFgaProvider requests"
            )
        }
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun writeRelationship(relationship: FgaRelTuple) {
        TODO("Not yet implemented")
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun deleteRelationships(relationships: List<FgaRelTuple>) {
        TODO("Not yet implemented")
    }

    /**
     * If you want to check many at once use `batchCheckRelationships`
     *
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun checkRelationship(relationship: FgaRelTuple): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * @throws OpenFgaProviderException
     * @throws FgaProviderException
     */
    override fun listObjects(
        actor: Pair<EntityName, ID>,
        relation: String,
        type: EntityName,
    ): List<String> {
        TODO("Not yet implemented")
    }
}
