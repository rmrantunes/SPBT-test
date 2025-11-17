package co.bondspot.spbttest.application.exception

import co.bondspot.spbttest.shared.enumeration.HttpStatusCode

/** Arbitrary exceptions thrown by the developer from application services executions. */
class ApplicationServiceException(errorMessage: String) : Exception() {
    val errors: List<String> = listOf(errorMessage)
    var httpStatusCode: Int = 400

    /**
     * In order to inform the presentation user of application the closest related HTTP status code
     * to the thrown exception.
     */
    fun relatedHttpStatusCode(lambda: HttpStatusCode.() -> Int): ApplicationServiceException {
        this.httpStatusCode = lambda(HttpStatusCode)
        return this
    }
}