package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.dto.*
import co.bondspot.spbttest.springweb.service.TaskService
import co.bondspot.spbttest.springweb.util.security.toAccount
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/task")
class TaskController(private val taskService: TaskService) {
    @PostMapping
    fun create(@Valid @RequestBody body: CreateTaskReqDto, @AuthenticationPrincipal jwt: Jwt): ResponseEntity<Task> {
        val created = taskService.create(body.toDomainEntity(), jwt.toAccount())
        return ResponseEntity.created(URI("/task/${created.id}")).body(created)
    }

    @GetMapping
    fun list(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<Task>> {
        val tasks = taskService.list(jwt.toAccount())
        return ResponseEntity.ok().body(tasks)
    }

    @GetMapping("/{id}")
    fun list(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<Task?> {
        val task = taskService.getById(id, jwt.toAccount())
        return ResponseEntity.ok().body(task)
    }

    @PatchMapping("/{id}/details")
    fun updateDetails(
        @PathVariable id: String,
        @Valid @RequestBody body: UpdateTaskDetailsReqDto,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UpdateTaskDetailsResDto> {
        val updated = taskService.updateDetails(id, body.toDomainEntity().title, jwt.toAccount())
        return ResponseEntity.ok().body(UpdateTaskDetailsResDto(updated))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @Valid @RequestBody body: UpdateTaskStatusReqDto,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<UpdateTaskStatusResDto> {
        val updated = taskService.updateStatus(id, body.toDomainEntity().status, jwt.toAccount())
        return ResponseEntity.ok().body(UpdateTaskStatusResDto(updated))
    }
}