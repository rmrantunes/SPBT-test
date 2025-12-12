package co.bondspot.spbttest.domain.exception

import java.lang.RuntimeException

open class FullTextSearchProviderException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
