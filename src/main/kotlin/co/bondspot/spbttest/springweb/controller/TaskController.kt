package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.service.TaskService
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
    fun create(@RequestBody task: Task): ResponseEntity<Task> {
        val created = taskService.create(task)
        return ResponseEntity.created(URI("/task/${created.id}")).body(created)
    }
}