package co.bondspot.spbttest.springweb.configuration

import co.bondspot.spbttest.application.exception.ApplicationServiceException
import co.bondspot.spbttest.shared.dto.ErrorDto
import co.bondspot.spbttest.shared.dto.ErrorResponseDto
import co.bondspot.spbttest.shared.dto.KnownErrorDtoType
import kotlinx.serialization.SerializationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponseDto> {
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

        return ResponseEntity.badRequest()
            .body(ErrorResponseDto(errors = errors, HttpStatus.BAD_REQUEST.value()))
    }

    @ExceptionHandler(ApplicationServiceException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleExceptions(ex: ApplicationServiceException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity.status(ex.relatedHttpStatusCode)
            .body(
                ErrorResponseDto(
                    statusCode = ex.relatedHttpStatusCode,
                    errors =
                        ex.errors.map {
                            ErrorDto(it, HttpStatus.valueOf(ex.relatedHttpStatusCode).name)
                        },
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentExceptions(
        ex: IllegalArgumentException
    ): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity.badRequest()
            .body(
                ErrorResponseDto(
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleSerializationExceptions(
        ex: SerializationException
    ): ResponseEntity<ErrorResponseDto> {
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
        return ResponseEntity.badRequest()
            .body(ErrorResponseDto(errors = errors, HttpStatus.BAD_REQUEST.value()))
    }
}
