package co.bondspot.spbttest.domain.contract

interface IFullTextSearchProvider {
    /** Index records for Full-text Search */
    fun <T> index(items: List<T>)

    fun <T> search(query: String, ids: List<String>? = null): List<T>
}
