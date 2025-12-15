package co.bondspot.spbttest.springweb.controller

import co.bondspot.spbttest.shared.dto.ResponseDto
import co.bondspot.spbttest.springweb.dto.CreateTaskReqDto
import co.bondspot.spbttest.springweb.dto.CreateTaskResDto
import co.bondspot.spbttest.springweb.dto.GetTaskResDto
import co.bondspot.spbttest.springweb.dto.ListTaskRelatedAccountsResDto
import co.bondspot.spbttest.springweb.dto.ListTasksResDto
import co.bondspot.spbttest.springweb.dto.OperationSuccessfulResDto
import co.bondspot.spbttest.springweb.dto.ShareTaskReqDto
import co.bondspot.spbttest.springweb.dto.UpdateTaskDetailsReqDto
import co.bondspot.spbttest.springweb.dto.UpdateTaskStatusReqDto
import co.bondspot.spbttest.springweb.service.TaskService
import co.bondspot.spbttest.springweb.util.security.toAccount
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.URI
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/task")
@SecurityRequirement(name = "bearerJwt")
@Tag(name = "Task")
class TaskController(private val taskService: TaskService) {
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
    fun list(
        @AuthenticationPrincipal jwt: Jwt,
        @Param("q") q: String?,
    ): ResponseEntity<ResponseDto<ListTasksResDto>> {
        val tasks = taskService.list(queryTerm = q, reqAccount = jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(ListTasksResDto(tasks)))
    }

    @PreAuthorize(
        "hasAnyAuthority('ROLE_admin', 'ROLE_somewhat-admin', 'ROLE_@spbttest-api:testizin')"
    )
    @GetMapping("/{id}")
    fun getById(
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
    ): ResponseEntity<ResponseDto<OperationSuccessfulResDto>> {
        val updated = taskService.updateDetails(id, body.toDomainEntity().title, jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(OperationSuccessfulResDto(updated)))
    }

    @PreAuthorize(
        "hasAnyAuthority('ROLE_admin', 'ROLE_somewhat-admin', 'ROLE_@spbttest-api:testizin')"
    )
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @Valid @RequestBody body: UpdateTaskStatusReqDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResponseDto<OperationSuccessfulResDto>> {
        val updated = taskService.updateStatus(id, body.toDomainEntity().status, jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(OperationSuccessfulResDto(updated)))
    }

    @PreAuthorize(
        "hasAnyAuthority('ROLE_admin', 'ROLE_somewhat-admin', 'ROLE_@spbttest-api:testizin')"
    )
    @PostMapping("/{id}/share")
    fun shareWith(
        @PathVariable id: String,
        @Valid @RequestBody body: ShareTaskReqDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResponseDto<OperationSuccessfulResDto>> {
        val updated =
            taskService.shareWith(
                id,
                body.accountIdToShareWith.dangerouslyForceCast(),
                body.relation.value ?: "viewer",
                jwt.toAccount(),
            )
        return ResponseEntity.ok().body(ResponseDto(OperationSuccessfulResDto(updated)))
    }

    @GetMapping("/{id}/shared-with")
    fun listRelatedAccounts(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<ResponseDto<ListTaskRelatedAccountsResDto>> {
        val accounts = taskService.listRelatedAccounts(id, jwt.toAccount())
        return ResponseEntity.ok().body(ResponseDto(ListTaskRelatedAccountsResDto(accounts)))
    }
}
