package co.bondspot.spbttest.domain.repository

import co.bondspot.spbttest.domain.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, String>