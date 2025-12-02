package co.bondspot.spbttest.domain.exception

open class IAMProviderException(message: String? = null, val relatedHttpStatusCode: Int = 500) :
    Exception(message)
