package co.bondspot.spbttest.springweb.persistence

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Message
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "messages")
data class AccountEntity(
    @Column(nullable = false)
    val username: String,
    @Column(nullable = false)
    val email: String,
    @Column("first_name", nullable = false)
    val firstName: String,
    @Column("last_name")
    val lastName: String? = null,
    @Column("iam_account_id")
    val iamAccountId: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null
) {
    companion object {
        fun fromDomain(domain: Account) = AccountEntity(
            username = domain.username,
            email = domain.email,
            firstName = domain.firstName,
            lastName = domain.lastName,
            iamAccountId = domain.iamAccountId,
            id = domain.id
        )
    }

    fun toDomain(): Account = Account(username, email, firstName, lastName, iamAccountId, id)
}

@Repository
interface AccountRepository : JpaRepository<AccountEntity, String> {
    fun findByEmail(email: String): AccountEntity?
}