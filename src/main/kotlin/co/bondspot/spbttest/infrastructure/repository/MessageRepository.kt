package co.bondspot.spbttest.infrastructure.repository

import co.bondspot.spbttest.infrastructure.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<MessageEntity, String>