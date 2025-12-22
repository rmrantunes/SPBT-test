package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.entity.FtsSearchResponse
import co.bondspot.spbttest.domain.entity.NotificationSubscription
import co.bondspot.spbttest.domain.entity.RevalRule
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NotificationSubscriptionServiceTests {
    val accountId = UUID.randomUUID().toString()
    val entityUid = "task:${UUID.randomUUID()}"

    lateinit var service: NotificationSubscriptionService
    lateinit var fts: IFullTextSearchProvider

    @BeforeEach
    fun setup() {
        fts = spyk<IFullTextSearchProvider>()
        service = NotificationSubscriptionService(fts)
    }

    @Nested
    inner class Create {
        @Test
        fun `invoke correct functions`() {
            val sub =
                NotificationSubscription(
                    accountId = accountId,
                    type = NotificationSubscription.Type.ENTITY_EVENTS,
                    events = listOf("*"),
                    entityUid = entityUid,
                    revalLevel = NotificationSubscription.RevalidationLevel.HIGH,
                    revalRules = listOf(RevalRule("fga", mapOf("relation" to "OWNER"))),
                )
            service.create(sub)

            verify {
                fts invoke
                    "index" withArguments
                    listOf(NotificationSubscription.ENTITY_NAME, listOf(sub))
            }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `invoke correct functions`() {
            val entityUid = "task:${UUID.randomUUID()}"
            val sub =
                NotificationSubscription(
                    accountId = accountId,
                    type = NotificationSubscription.Type.ENTITY_EVENTS,
                    events = listOf("*"),
                    entityUid = entityUid,
                    revalLevel = NotificationSubscription.RevalidationLevel.HIGH,
                    revalRules = listOf(RevalRule("fga", mapOf("relation" to "OWNER"))),
                )

            service.create(sub)

            val id = "${accountId}_$entityUid".replace(":", "_")
            service.delete(id)

            verify {
                fts invoke "delete" withArguments listOf(NotificationSubscription.ENTITY_NAME, id)
            }
        }
    }

    @Nested
    inner class FindAccounts {
        @Test
        fun `invoke correct functions`() {
            val entityUid = "task:${UUID.randomUUID()}"
            val sub =
                NotificationSubscription(
                    accountId = accountId,
                    type = NotificationSubscription.Type.ENTITY_EVENTS,
                    events = listOf("*"),
                    entityUid = entityUid,
                    revalLevel = NotificationSubscription.RevalidationLevel.HIGH,
                    revalRules = listOf(RevalRule("fga", mapOf("relation" to "owner"))),
                )

            every {
                fts.search(
                    NotificationSubscription.ENTITY_NAME,
                    "",
                    null,
                    listOf("entityUid = \"$entityUid\"", listOf("event = \"*\"")),
                )
            } returns FtsSearchResponse(listOf(mapOf("accountId" to accountId)))

            service.create(sub)

            // TODO we still need to be able to process (a possible) 1M accounts subscribed
            //  So we can add a pagination + some coroutines (and as well stream the http response
            // to
            //  not load all objects at once)
            //  All that with retry.
            val result = service.findAccounts(entityUid = entityUid)

            assertThat(result.accountsIds).containsAll(listOf(accountId))
        }
    }
}
