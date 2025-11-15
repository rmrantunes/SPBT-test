package co.bondspot.spbttest.springweb.dto

import co.bondspot.spbttest.domain.entity.Task
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskReqDto(
    @field:NotBlank(message = "is required")
    @field:Size(min = 1, max = 160)
    val title: String = "",
) {
    fun toDomainEntity(): Task = Task(title)
}
