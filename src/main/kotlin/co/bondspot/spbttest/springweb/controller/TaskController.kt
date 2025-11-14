package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.dto.CreateTaskReqDto
import co.bondspot.spbttest.springweb.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/task")
class TaskController(private val taskService: TaskService) {
    @PostMapping
    fun create(@Valid @RequestBody body: CreateTaskReqDto): ResponseEntity<Task> {
        val created = taskService.create(body.toDomainEntity())
        return ResponseEntity.created(URI("/task/${created.id}")).body(created)
    }
}