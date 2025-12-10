package co.bondspot.spbttest.domain.contract

interface IFullTextSearchProvider {
    /** Index records for Full-text Search */
    fun <T> index(tasks: List<T>)

    fun <T> fullTextSearch(query: String): List<T>
}