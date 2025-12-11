package co.bondspot.spbttest.infrastructure.fts

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider

class MeilisearchProvider : IFullTextSearchProvider {
    override fun <T> index(collection: String, items: List<T>) {
        TODO("Not yet implemented")
    }

    override fun <T> search(collection: String, query: String, ids: List<String>?): List<T> {
        TODO("Not yet implemented")
    }
}
