package co.bondspot.spbttest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/")
class MessageController(private val messageService: MessageService) {
    @GetMapping("/")
    fun listMessages(): List<Message> = messageService.findMessages()

    @GetMapping("/{id}")
    fun getMessage(@PathVariable id: String): ResponseEntity<Message> =
        messageService.findMessageById(id).toResponseEntity()

    @PostMapping
    fun createMessage(@RequestBody message: Message): ResponseEntity<Message> {
        val savedMessage = messageService.save(message)
        return ResponseEntity.created(URI("/${savedMessage.id}")).body(savedMessage)
    }

    private fun Message?.toResponseEntity(): ResponseEntity<Message> {
        return this?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}