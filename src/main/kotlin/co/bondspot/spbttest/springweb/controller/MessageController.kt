package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Message
import co.bondspot.spbttest.springweb.service.MessageService
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/message")
class MessageController(private val messageService: MessageService) {
    @GetMapping("/") fun listMessages(): List<Message> = messageService.find()

    @GetMapping("/{id}")
    fun getMessage(@PathVariable id: String): ResponseEntity<Message> =
        messageService.findById(id).toResponseEntity()

    @PostMapping
    fun createMessage(@RequestBody messageEntity: Message): ResponseEntity<Message> {
        val savedMessage = messageService.save(messageEntity)
        return ResponseEntity.created(URI("/message/${savedMessage.id}")).body(savedMessage)
    }

    private fun Message?.toResponseEntity(): ResponseEntity<Message> {
        return this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}
