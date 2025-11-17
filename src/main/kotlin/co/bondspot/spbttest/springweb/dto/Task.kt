package co.bondspot.spbttest.springweb.dto

import co.bondspot.spbttest.domain.entity.Task
import co.bondspot.spbttest.springweb.util.validation.IsString
import co.bondspot.spbttest.springweb.util.validation.KSVString
import co.bondspot.spbttest.springweb.util.validation.KSVerifiable
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskReqDto(
    @IsString
    @Serializable(KSVString::class)
    val title: KSVerifiable<String> = KSVerifiable(),

    @IsString(nullable = true, required = false)
    @Serializable(KSVString::class)
    val description: KSVerifiable<String?> = KSVerifiable(),
) {
    fun toDomainEntity(): Task = Task(title.dangerouslyForceCast(), description = description.value)
}

@Serializable
data class UpdateTaskDetailsReqDto(
    @IsString(nullable = true, required = false)
    @Serializable(KSVString::class)
    val title: KSVerifiable<String> = KSVerifiable(),

    @IsString(nullable = true, required = false)
    @Serializable(KSVString::class)
    val description: KSVerifiable<String?> = KSVerifiable(),
) {
    fun toDomainEntity(): Task = Task(title.dangerouslyForceCast(), description = description.value)
}

@Serializable
data class UpdateTaskDetailsResDto(
    val updated: Boolean?,
)
