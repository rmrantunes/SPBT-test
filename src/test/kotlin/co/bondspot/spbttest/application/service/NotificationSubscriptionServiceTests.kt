package co.bondspot.spbttest.application.service

import co.bondspot.spbttest.domain.contract.IFullTextSearchProvider
import co.bondspot.spbttest.domain.entity.NotificationSubscription
import co.bondspot.spbttest.domain.entity.RevalRule
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
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
}
