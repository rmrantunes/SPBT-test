package co.bondspot.spbttest.springweb.configuration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
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

    @ExceptionHandler(JsonProcessingException::class)
    fun handleJsonProcessingException(ex: JsonProcessingException): ResponseEntity<ResponseDto> {
        val errors = buildList {
            if (ex is MismatchedInputException && ex.path.isNotEmpty()) {
                val fieldName = ex.path.last().fieldName ?: "unknown field"
                val expectedType = ex.targetType?.simpleName ?: "from the docs"
                add("'$fieldName' must be of type $expectedType")
            } else {
                add("Invalid JSON structure: ${ex.message ?: "Type mismatch in request"}. Please, check the docs for details.")
            }
        }

        return ResponseEntity.badRequest().body(ResponseDto(errors = errors))
    }
}