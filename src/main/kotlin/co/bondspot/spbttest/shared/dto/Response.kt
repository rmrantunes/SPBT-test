package co.bondspot.spbttest.shared.dto

enum class KnownErrorDtoType {
    UNACCEPTABLE_INPUT_STATE,
    SERIALIZATION_EXCEPTION,
    ILLEGAL_ARGUMENT_EXCEPTION
}

data class ErrorDto(val message: String, val type: String)

data class ErrorResponseDto(val errors: List<ErrorDto> = emptyList(), val statusCode: Int = 500)