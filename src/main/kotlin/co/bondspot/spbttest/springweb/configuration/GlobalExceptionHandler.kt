package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import kotlinx.serialization.SerializationException
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ResponseDto(val errors: List<String> = emptyList())

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ResponseDto> {
        val errors = buildList {
            ex.bindingResult.allErrors.forEach { error ->
                if (error is FieldError) add("'${error.field}' ${error.defaultMessage ?: "invalid field"}")
                else error.defaultMessage?.let { add(it) }
            }
        }.sorted()

        return ResponseEntity.badRequest().body(ResponseDto(errors = errors))
    }

    @ExceptionHandler(ApplicationServiceException::class)
    fun handleExceptions(ex: ApplicationServiceException): ResponseEntity<ResponseDto> {
        return ResponseEntity
            .status(ex.relatedHttpStatusCode)
            .body(ResponseDto(errors = ex.errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentExceptions(ex: IllegalArgumentException): ResponseEntity<ResponseDto> {
        return ResponseEntity.badRequest().body(ResponseDto(errors = listOf(ex.message!!)))
    }

    @ExceptionHandler(SerializationException::class)
    fun handleSerializationExceptions(ex: SerializationException): ResponseEntity<ResponseDto> {
        val message =
            (ex.message ?: "JSON deserialization failed (e.g., malformed JSON or type mismatch or missing field)")
                // .replace(Regex("\\$.([\\w.]*)"), "\'$1\'")
                .split("\nJSON input:")[0]
        val errors = listOf("$message. Please, check the docs for details.")
        return ResponseEntity.badRequest().body(ResponseDto(errors = errors))
    }
}