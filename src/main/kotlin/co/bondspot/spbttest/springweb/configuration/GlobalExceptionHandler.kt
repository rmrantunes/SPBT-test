package co.bondspot.spbttest.springweb.configuration

import org.springframework.http.HttpStatus
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
        val errors = buildList<String> {
            ex.bindingResult.allErrors.forEach { error ->
                if (error is FieldError) add("'${error.field}' ${error.defaultMessage ?: "invalid field"}")
                else error.defaultMessage?.let { add(it) }

            }
        }

        return ResponseEntity.badRequest().body(ResponseDto(errors = errors))
    }
}