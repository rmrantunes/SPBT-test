package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.FtsSearchResponse

interface IFullTextSearchProvider {
    /** Index records for Full-text Search */
    fun index(indexUid: String, items: List<Any>)

    fun search(indexUid: String, query: String, ids: List<String>? = null): FtsSearchResponse
}
