package co.bondspot.spbttest.domain.contract

import co.bondspot.spbttest.domain.entity.FtsSearchResponse

interface IFullTextSearchProvider {
    /** Indexes records for Full-Text Search */
    fun index(indexUid: String, items: List<Any>)

    fun search(
        indexUid: String,
        query: String,
        ids: List<String>? = null,
        filter: List<Any>? = null,
        page: Int = 1,
        hitsPerPage: Int = 20,
    ): FtsSearchResponse

    /** Deletes an indexed record with given id */
    fun delete(indexUid: String, id: String)
}
