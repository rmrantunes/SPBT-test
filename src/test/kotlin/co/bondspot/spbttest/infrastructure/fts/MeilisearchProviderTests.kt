package co.bondspot.spbttest.infrastructure.fts

import co.bondspot.spbttest.testutils.KSelect
import org.instancio.Instancio
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.*
import kotlin.test.Test

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
            .set(KSelect.field(Foo::name), "some_name")
            .generate(KSelect.field(Foo::name)) { it.text().word().noun() }
            .generate(KSelect.field(Foo::keywords)) { it.collection<String>().size(4) }
            .create()

    @Nested
    @DisplayName("when indexing documents...")
    inner class IndexDocuments {

        @Test
        fun `create successfully`() {
            assertDoesNotThrow { meilisearch.index(Foo.ENTITY_NAME, listOf(generateFoo())) }
        }
    }

    @Nested
    @DisplayName("when searching documents...")
    inner class SearchDocuments {

        @Test
        fun `query successfully`() {
            assertDoesNotThrow { meilisearch.search<Foo>(Foo.ENTITY_NAME, "some_name") }
        }
    }
}
