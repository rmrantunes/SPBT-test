package co.bondspot.spbttest.infrastructure.fts

import co.bondspot.spbttest.domain.entity.FtsSearchResponse
import co.bondspot.spbttest.testutils.KSelect
import java.util.*
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.instancio.Instancio
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

data class Foo(val id: String, val name: String, val keywords: List<String>) {
    companion object {
        const val ENTITY_NAME = "foo"
    }
}

class MeillisearchProviderTests {
    val meilisearch = MeilisearchProvider()

    fun generateFoo(): Foo =
        Instancio.of(Foo::class.java)
            .set(KSelect.field(Foo::id), UUID.randomUUID().toString())
            .generate(KSelect.field(Foo::name)) { it.text().word().noun() }
            .generate(KSelect.field(Foo::keywords)) { it.collection<String>().size(4) }
            .create()

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            MeilisearchProvider().setFilterableAttributes(Foo.ENTITY_NAME, listOf("id"))
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            MeilisearchProvider().dangerouslyDeleteAllDocuments(Foo.ENTITY_NAME)
        }
    }

    @Nested
    @DisplayName("when indexing documents...")
    inner class IndexDocuments {

        @Test
        fun `create successfully`() {
            assertDoesNotThrow { meilisearch.index(Foo.ENTITY_NAME, listOf(generateFoo())) }
        }

        @Test
        fun `throw a certain exception with MeilisearchProviderException`() {
            val ex =
                assertThrows<MeilisearchProviderException> { meilisearch.index("aha", listOf("kllk")) }
            assertThat(ex).isExactlyInstanceOf(MeilisearchProviderException::class.java)
            assertThat(ex.message)
                .startsWith("Meilisearch API responded with error status code 500")
        }
    }

    @Nested
    @DisplayName("when searching documents...")
    inner class SearchDocuments {

        @Test
        fun `query successfully`() {
            val foo = generateFoo()
            val items = buildList {
                repeat(4) {
                    add(foo.copy(name = "${foo.name} #$it", id = UUID.randomUUID().toString()))
                }
            }

            meilisearch.index(Foo.ENTITY_NAME, items)

            val meilisearchTaskTimeoutMillis = 1000L
            Thread.sleep(meilisearchTaskTimeoutMillis)

            val result = meilisearch.search(Foo.ENTITY_NAME, foo.name)
            assertThat(result).isInstanceOf(FtsSearchResponse::class.java)
            assertThat(result.hits).hasSize(4)
        }
    }

    @Test
    fun `query successfully informing ids`() {
        val foo = generateFoo()
        val items = buildList {
            repeat(4) {
                add(foo.copy(name = "${foo.name} #$it", id = UUID.randomUUID().toString()))
            }
        }

        meilisearch.index(Foo.ENTITY_NAME, items)

        val itemsIds = items.map { it.id }.subList(0, 2)

        val meilisearchTaskTimeoutMillis = 1000L
        Thread.sleep(meilisearchTaskTimeoutMillis)

        val result = meilisearch.search(Foo.ENTITY_NAME, foo.name, itemsIds)
        assertThat(result).isInstanceOf(FtsSearchResponse::class.java)
        assertThat(result.hits).hasSize(2)
        assertThat(result.hitsIds()).containsExactlyElementsOf(itemsIds)
    }
}
