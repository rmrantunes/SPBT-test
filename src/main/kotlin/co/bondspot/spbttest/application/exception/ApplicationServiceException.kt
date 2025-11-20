package co.bondspot.spbttest.application.exception

import co.bondspot.spbttest.shared.enumeration.HttpStatusCode

/** Arbitrary exceptions thrown by the developer from application services executions. */
class ApplicationServiceException(errorMessage: String) : Exception(errorMessage) {
    val errors: List<String> = listOf(errorMessage)
    var relatedHttpStatusCode: Int = 400

    /**
     * In order to inform the user of the application layer the closest related HTTP status code
     * to the thrown exception.
     */
    fun setRelatedHttpStatusCode(lambda: HttpStatusCode.() -> Int): ApplicationServiceException {
        this.relatedHttpStatusCode = lambda(HttpStatusCode)
        return this
    }
}