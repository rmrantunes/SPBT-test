package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Account
import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.dto.*
import co.bondspot.spbttest.springweb.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/task")
class TaskController(private val taskService: TaskService) {
    @PostMapping
    fun create(@Valid @RequestBody body: CreateTaskReqDto): ResponseEntity<Task> {
        val created = taskService.create(body.toDomainEntity(), Account())
        return ResponseEntity.created(URI("/task/${created.id}")).body(created)
    }

    @GetMapping
    fun list(): ResponseEntity<List<Task>> {
        val tasks = taskService.list(Account())
        return ResponseEntity.ok().body(tasks)
    }

    @GetMapping("/{id}")
    fun list(
        @PathVariable id: String
    ): ResponseEntity<Task?> {
        val task = taskService.getById(id, Account())
        return ResponseEntity.ok().body(task)
    }

    @PatchMapping("/{id}/details")
    fun updateDetails(
        @PathVariable id: String,
        @Valid @RequestBody body: UpdateTaskDetailsReqDto
    ): ResponseEntity<UpdateTaskDetailsResDto> {
        val updated = taskService.updateDetails(id, body.toDomainEntity().title, Account())
        return ResponseEntity.ok().body(UpdateTaskDetailsResDto(updated))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @Valid @RequestBody body: UpdateTaskStatusReqDto
    ): ResponseEntity<UpdateTaskStatusResDto> {
        val updated = taskService.updateStatus(id, body.toDomainEntity().status, Account())
        return ResponseEntity.ok().body(UpdateTaskStatusResDto(updated))
    }
}