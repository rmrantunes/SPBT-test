package co.bondspot.spbttest.springweb.dto

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.util.validation.IsOneOf
import co.bondspot.spbttest.springweb.util.validation.IsString
import co.bondspot.spbttest.springweb.util.validation.KSVString
import co.bondspot.spbttest.springweb.util.validation.KSVerifiable
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskReqDto(
    @IsString
    @Serializable(KSVString::class)
    @field:Schema(type = "string", required = true)
    val title: KSVerifiable<String> = KSVerifiable(),
    @IsString(nullable = true, required = false)
    @Serializable(KSVString::class)
    @field:Schema(type = "string", nullable = true)
    val description: KSVerifiable<String?> = KSVerifiable(),
) {
    fun toDomainEntity(): Task = Task(title.dangerouslyForceCast(), description = description.value)
}

@Serializable
data class UpdateTaskDetailsReqDto(
    @IsString(nullable = true, required = false)
    @Serializable(KSVString::class)
    @field:Schema(type = "string", nullable = true)
    val title: KSVerifiable<String> = KSVerifiable(),
    @IsString(nullable = true, required = false)
    @Serializable(KSVString::class)
    @field:Schema(type = "string", nullable = true)
    val description: KSVerifiable<String?> = KSVerifiable(),
) {
    fun toDomainEntity(): Task = Task(title.value ?: "", description = description.value)
}

@Serializable data class UpdateTaskDetailsResDto(val updated: Boolean?)

@Serializable
data class UpdateTaskStatusReqDto(
    @IsString
    @IsOneOf(["PENDING", "IN_PROGRESS", "COMPLETED"])
    @Serializable(KSVString::class)
    @field:Schema(implementation = Task.Status::class)
    val status: KSVerifiable<String> = KSVerifiable()
) {
    fun toDomainEntity() = Task(status = Task.Status.valueOf(status.dangerouslyForceCast()))
}

@Serializable data class UpdateTaskStatusResDto(val updated: Boolean?)
