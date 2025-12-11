package co.bondspot.spbttest.infrastructure.fts

import co.bondspot.spbttest.testutils.KSelect
import java.util.UUID
import kotlin.test.Test
import org.instancio.Instancio
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow

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

    @Nested
    @DisplayName("when indexing documents...")
    inner class IndexDocuments {

        @Test
        fun `create successfully`() {
            assertDoesNotThrow { meilisearch.index(Foo.ENTITY_NAME, listOf(generateFoo())) }
        }
    }
}
