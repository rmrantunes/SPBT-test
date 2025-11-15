package co.bondspot.spbttest.springweb.util.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IsStringValidator::class])
annotation class IsString(
    val message: String = "must be a string",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val nullable: Boolean = false,
)

class IsStringValidator : ConstraintValidator<IsString, Any?> {
    private var nullable = false
    private var message: String? = null

    override fun initialize(constraintAnnotation: IsString?) {
        nullable = constraintAnnotation?.nullable == true
        message = constraintAnnotation?.message
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        val v = if (value is KSVerifiable<*>) value.value else value
        return handleNullable(value, context, nullable, message) && v is String
    }
}

private fun handleNullable(
    value: Any?,
    context: ConstraintValidatorContext?,
    nullable: Boolean,
    message: String? = null
): Boolean {
    val isNullValue = if (value is KSVerifiable<*>) value.value == null && !value.isUndefined()
    else value == null

    val isNullableAndNotNull = !isNullValue && nullable

    if (isNullableAndNotNull) {
        val messageTemplate = if (message != null) "$message or null" else "can be a null value"

        context?.disableDefaultConstraintViolation()
        context?.buildConstraintViolationWithTemplate(
            messageTemplate,
        )?.addConstraintViolation()
        return false
    }

    return true
}
