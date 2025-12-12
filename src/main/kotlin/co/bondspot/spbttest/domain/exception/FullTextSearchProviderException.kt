package co.bondspot.spbttest.domain.exception

import java.lang.RuntimeException

class FullTextSearchProviderException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
