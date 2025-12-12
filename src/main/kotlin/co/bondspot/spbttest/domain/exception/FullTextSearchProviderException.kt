package co.bondspot.spbttest.domain.exception

import java.lang.RuntimeException

open class FullTextSearchProviderException(
    message: String,
    cause: Throwable? = null,
    open val contextParams: Map<String, Any> = emptyMap(),
) : RuntimeException(message, cause)
