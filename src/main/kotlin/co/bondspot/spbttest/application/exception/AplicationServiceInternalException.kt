package co.bondspot.spbttest.application.exception

import co.bondspot.spbttest.shared.enumeration.HttpStatusCode5xx

/**
 * Arbitrary exceptions thrown by the developer from application services executions. With messages
 * directed to the developer about NEVER cases.
 */
class ApplicationServiceInternalException(errorMessage: String) :
    Exception("This should NOT be happening at all: $errorMessage") {
    var relatedHttpStatusCode: Int = 500

    /**
     * In order to inform the user of the application layer the closest related HTTP status code to
     * the thrown exception.
     */
    fun setRelatedHttpStatusCode(
        lambda: HttpStatusCode5xx.() -> Int
    ): ApplicationServiceInternalException {
        this.relatedHttpStatusCode = lambda(HttpStatusCode5xx)
        return this
    }
}
