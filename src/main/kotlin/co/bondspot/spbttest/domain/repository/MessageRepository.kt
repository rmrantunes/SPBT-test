package co.bondspot.spbttest.domain.repository

import co.bondspot.spbttest.domain.entity.Message
import org.springframework.data.repository.CrudRepository

interface MessageRepository : CrudRepository<Message, String>