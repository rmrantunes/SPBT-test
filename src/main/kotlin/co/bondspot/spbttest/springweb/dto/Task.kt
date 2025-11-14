package co.bondspot.spbttest.springweb.dto

import co.bondspot.spbttest.domain.entity.Task
import jakarta.validation.constraints.NotBlank

data class CreateTaskReqDto(
    @field:NotBlank
    val title: String

) {
    fun toDomainEntity(): Task = Task(title)
}
