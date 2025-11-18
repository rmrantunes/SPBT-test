package co.bondspot.spbttest.springweb.service

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.domain.contract.AccountRepositoryContract
import co.bondspot.spbttest.domain.contract.IAMProviderContract
import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.IAMAccount
import co.bondspot.spbttest.shared.enumeration.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.Ignore

class AccountServiceTests {
    @Nested
    @DisplayName("when registering an account")
    inner class RegisterAccount {
        @Test
        fun `return FORBIDDEN if user with same email already exists in IAM or DB`() {
            val iamProvider = mockk<IAMProviderContract>()
            val accountRepository = mockk<AccountRepositoryContract>()
            val service = AccountService(accountRepository, iamProvider)

            val account = Account("haha@email.com", "Haha", "O Monstro")
            val iamAccount = IAMAccount("haha@email.com", "Haha", "O Monstro")

            every { iamProvider.getByEmail(account.email) } returns iamAccount
            every { accountRepository.getByEmail(account.email) } returns account

            val exception = assertThrows<ApplicationServiceException> {
                service.register(
                    account,
                    "pas123"
                )
            }

            assertThat(exception.message).isEqualTo("Account already exists")
            assertThat(exception.errors[0]).isEqualTo("Account already exists")
            assertThat(exception.httpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)

            every { iamProvider.getByEmail(account.email) } returns null

            val exception2 = assertThrows<ApplicationServiceException> {
                service.register(
                    account,
                    "pas123"
                )
            }

            assertThat(exception2.message).isEqualTo("Account already exists")
            assertThat(exception2.errors[0]).isEqualTo("Account already exists")
            assertThat(exception2.httpStatusCode).isEqualTo(HttpStatusCode.FORBIDDEN)
        }

        @Ignore
        fun `should register a new account with IAM integration`() {
        }
    }
}