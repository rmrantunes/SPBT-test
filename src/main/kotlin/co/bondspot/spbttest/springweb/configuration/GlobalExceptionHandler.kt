package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.shared.dto.ErrorDto
import co.bondspot.spbttest.shared.dto.KnownErrorDtoType
import co.bondspot.spbttest.shared.dto.ResponseDto
import kotlinx.serialization.SerializationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ResponseDto> {
        val errors =
            buildList {
                    ex.bindingResult.allErrors.forEach { error ->
                        if (error is FieldError)
                            add(
                                ErrorDto(
                                    "'${error.field}' ${error.defaultMessage ?: "invalid field"}",
                                    KnownErrorDtoType.UNACCEPTABLE_INPUT_STATE.name,
                                )
                            )
                        else
                            error.defaultMessage?.let {
                                add(ErrorDto(it, KnownErrorDtoType.UNACCEPTABLE_INPUT_STATE.name))
                            }
                    }
                }
                .sortedBy { it.message }

        return ResponseEntity.badRequest().body(ResponseDto(errors = errors, HttpStatus.BAD_REQUEST.value()))
    }

    @ExceptionHandler(ApplicationServiceException::class)
    fun handleExceptions(ex: ApplicationServiceException): ResponseEntity<ResponseDto> {
        return ResponseEntity.status(ex.relatedHttpStatusCode)
            .body(
                ResponseDto(
                    statusCode = ex.relatedHttpStatusCode,
                    errors =
                        ex.errors.map {
                            ErrorDto(it, HttpStatus.valueOf(ex.relatedHttpStatusCode).name)
                        }
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentExceptions(ex: IllegalArgumentException): ResponseEntity<ResponseDto> {
        return ResponseEntity.badRequest()
            .body(
                ResponseDto(
                    errors =
                        listOf(
                            ErrorDto(
                                ex.message!!,
                                KnownErrorDtoType.ILLEGAL_ARGUMENT_EXCEPTION.name,
                            )
                        )
                )
            )
    }

    @ExceptionHandler(SerializationException::class)
    fun handleSerializationExceptions(ex: SerializationException): ResponseEntity<ResponseDto> {
        val message =
            (ex.message
                    ?: "JSON deserialization failed (e.g., malformed JSON or type mismatch or missing field)")
                // .replace(Regex("\\$.([\\w.]*)"), "\'$1\'")
                .split("\nJSON input:")[0]
        val errors =
            listOf(
                ErrorDto(
                    message = "$message. Please, check the docs for details.",
                    type = KnownErrorDtoType.SERIALIZATION_EXCEPTION.name,
                )
            )
        return ResponseEntity.badRequest().body(ResponseDto(errors = errors, HttpStatus.BAD_REQUEST.value()))
    }
}
