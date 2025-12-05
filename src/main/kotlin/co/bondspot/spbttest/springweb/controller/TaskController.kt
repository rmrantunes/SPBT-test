package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.shared.dto.ResponseDto
import co.bondspot.spbttest.springweb.dto.*
import co.bondspot.spbttest.springweb.service.TaskService
import co.bondspot.spbttest.springweb.util.security.toAccount
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/task")
@SecurityRequirement(name = "bearerJwt")
@Tag(name = "Task")
class TaskController(
    private val taskService: TaskService
) {
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_admin', 'ROLE_somewhat-admin')")
    fun create(
        @Valid @RequestBody body: CreateTaskReqDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResponseDto<CreateTaskResDto>> {
        val created = taskService.create(body.toDomainEntity(), jwt.toAccount())
        return ResponseEntity.created(URI("/task/${created.id}"))
            .body(ResponseDto(CreateTaskResDto(created)))
    }

    @GetMapping
    fun list(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<ResponseDto<ListTasksResDto>> {
        val tasks = taskService.list(jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(ListTasksResDto(tasks)))
    }

    @PreAuthorize(
        "hasAnyAuthority('ROLE_admin', 'ROLE_somewhat-admin', 'ROLE_@spbttest-api:testizin')"
    )
    @GetMapping("/{id}")
    fun list(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResponseDto<GetTaskResDto>> {
        val task = taskService.getById(id, jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(GetTaskResDto(task)))
    }

    @PreAuthorize(
        "hasAnyAuthority('ROLE_admin', 'ROLE_somewhat-admin', 'ROLE_@spbttest-api:testizin')"
    )
    @PatchMapping("/{id}/details")
    fun updateDetails(
        @PathVariable id: String,
        @Valid @RequestBody body: UpdateTaskDetailsReqDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResponseDto<UpdateTaskDetailsResDto>> {
        val updated = taskService.updateDetails(id, body.toDomainEntity().title, jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(UpdateTaskDetailsResDto(updated)))
    }

    @PreAuthorize(
        "hasAnyAuthority('ROLE_admin', 'ROLE_somewhat-admin', 'ROLE_@spbttest-api:testizin')"
    )
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @Valid @RequestBody body: UpdateTaskStatusReqDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResponseDto<UpdateTaskStatusResDto>> {
        val updated = taskService.updateStatus(id, body.toDomainEntity().status, jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(UpdateTaskStatusResDto(updated)))
    }
}
