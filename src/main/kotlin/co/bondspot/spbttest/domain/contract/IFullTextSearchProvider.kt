package co.bondspot.spbttest.domain.contract

interface IFullTextSearchProvider {
    /** Index records for Full-text Search */
    fun <T> index(collection: String, items: List<T>)

    fun <T> search(collection: String, query: String, ids: List<String>? = null): List<T>
}
