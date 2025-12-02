package co.bondspot.spbttest.springweb.persistence

import co.bondspot.spbttest.domain.entity.Account
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "account")
data class AccountEntity(
    @Column(nullable = false)
    val username: String,
    @Column(nullable = false)
    val email: String,
    @Column("first_name", nullable = false)
    val firstName: String,
    @Column("last_name")
    val lastName: String? = null,
    @Id
    @Column(nullable = false, length = 60)
    val id: String? = null
) {
    companion object {
        fun fromDomain(domain: Account) = AccountEntity(
            username = domain.username,
            email = domain.email,
            firstName = domain.firstName,
            lastName = domain.lastName,
            id = domain.id
        )
    }

    fun toDomain(): Account = Account(username, email, firstName, lastName, id = id)
}

@Repository
interface AccountRepository : JpaRepository<AccountEntity, String> {
    fun findByEmail(email: String): AccountEntity?
    fun findByUsername(username: String): AccountEntity?
}