package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.AccountRepositoryContract
import co.bondspot.spbttest.domain.contract.IAMProviderContract
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.domain.exception.IAMProviderException
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class AccountServiceTests {
    @Nested
    @DisplayName("when registering an account...")
    inner class RegisterAccount {
        @Test
        fun `return CONFLICT if user with same username or email already exists in IAM or DB`() {
            val iamProvider = mockk<IAMProviderContract>()
            val accountRepository = mockk<AccountRepositoryContract>()
            val service = AccountService(accountRepository, iamProvider)

            val account = Account("user1", "haha@email.com", "Haha", "O Monstro")
            val iamAccount = IAMAccount("user1", "haha@email.com", "Haha", "O Monstro")

            every { iamProvider.getByUsername(account.username) } returns iamAccount
            every { accountRepository.getByUsername(account.username) } returns account
            every { iamProvider.getByEmail(account.email) } returns iamAccount
            every { accountRepository.getByEmail(account.email) } returns account

            val exception = assertThrows<ApplicationServiceException> {
                service.register(
                    account,
                    "pas123"
                )
            }

            val exMessage = "Account already exists"
            assertThat(exception.message).isEqualTo(exMessage)
            assertThat(exception.errors[0]).isEqualTo(exMessage)
            assertThat(exception.httpStatusCode).isEqualTo(HttpStatusCode.CONFLICT)

            every { iamProvider.getByUsername(account.username) } returns null
            every { accountRepository.getByUsername(account.username) } returns null

            val exception2 = assertThrows<ApplicationServiceException> {
                service.register(
                    account,
                    "pas123"
                )
            }

            assertThat(exception2.message).isEqualTo(exMessage)
            assertThat(exception2.errors[0]).isEqualTo(exMessage)
            assertThat(exception2.httpStatusCode).isEqualTo(HttpStatusCode.CONFLICT)

            every { iamProvider.getByEmail(account.email) } returns null

            val exception3 = assertThrows<ApplicationServiceException> {
                service.register(
                    account,
                    "pas123"
                )
            }

            assertThat(exception3.message).isEqualTo(exMessage)
            assertThat(exception3.errors[0]).isEqualTo(exMessage)
            assertThat(exception3.httpStatusCode).isEqualTo(HttpStatusCode.CONFLICT)
        }

        @Test
        fun `if valid should register to DB with IAM integration`() {
            val iamProvider = mockk<IAMProviderContract>()
            val accountRepository = mockk<AccountRepositoryContract>()
            val service = AccountService(accountRepository, iamProvider)

            val accountId = UUID.randomUUID().toString()
            val iamAccountId = UUID.randomUUID().toString()

            val account = Account("haha@email.com", "Haha", "O Monstro")
            val iamAccount = IAMAccount("haha@email.com", "Haha", "O Monstro")

            every { iamProvider.getByEmail(account.email) } returns null
            every { accountRepository.getByEmail(account.email) } returns null
            every { iamProvider.getByUsername(account.username) } returns null
            every { accountRepository.getByUsername(account.username) } returns null

            every { iamProvider.register(iamAccount, "pas123") } returns iamAccount.copy(id = iamAccountId)
            every { accountRepository.register(account.copy(iamAccountId = iamAccountId)) } answers {
                (firstArg() as Account).copy(
                    id = accountId
                )
            }

            every { iamProvider.setExternalId(iamAccountId, accountId) } returns Unit

            service.register(account, "pas123")

            verify { iamProvider.register(iamAccount, "pas123") }
            verify { iamProvider.setExternalId(iamAccountId, accountId) }
        }
    }

    @Nested
    @DisplayName("when obtaining an account access token...")
    inner class ObtainAccountAccessToken {
        @Test
        fun `if invalid throw ApplicationServiceException`() {
            val iamProvider = mockk<IAMProviderContract>()
            val accountRepository = mockk<AccountRepositoryContract>()
            val service = AccountService(accountRepository, iamProvider)

            every { iamProvider.obtainAccessToken(any(), any()) } throws IAMProviderException(
                "Invalid account credentials",
                HttpStatusCode.UNAUTHORIZED
            )

            val ex = assertThrows<ApplicationServiceException> {
                service.obtainAccessToken(
                    "not_the_username",
                    "not_the_password"
                )
            }
            assertThat(ex.message).isEqualTo("Invalid account credentials")
            assertThat(ex.httpStatusCode).isEqualTo(HttpStatusCode.UNAUTHORIZED)
        }
    }
}