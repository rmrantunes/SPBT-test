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
    val required: Boolean = true,
)

class IsStringValidator : ConstraintValidator<IsString, Any?> {
    private var nullable = false
    private var required = true
    private var message: String? = null

    override fun initialize(constraintAnnotation: IsString?) {
        nullable = constraintAnnotation?.nullable == true
        required = constraintAnnotation?.required == true
        message = constraintAnnotation?.message
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        if (canBypassRequired(value, required)) return true

        if (!checkRequired(value, context, required, nullable, message)) return false

        val v = if (value is KSVerifiable<*>) value.value else value

        if (!checkNullable(value, context, nullable, message)) return false

        return v is String
    }
}

private fun canBypassRequired(value: Any?, required: Boolean): Boolean {
    return !required && value is KSVerifiable<*> && value.isUndefined()
}

private fun checkRequired(
    value: Any?,
    context: ConstraintValidatorContext?,
    required: Boolean,
    nullable: Boolean,
    message: String? = null
): Boolean {
    if (required && value is KSVerifiable<*> && value.isUndefined()) {
        val messageTemplate = if (message != null) if (nullable) "$message or null" else message else "is required"

        context?.disableDefaultConstraintViolation()
        context?.buildConstraintViolationWithTemplate(
            messageTemplate,
        )?.addConstraintViolation()

        return false
    }
    return true
}

private fun checkNullable(
    value: Any?,
    context: ConstraintValidatorContext?,
    nullable: Boolean,
    message: String? = null
): Boolean {
    val isNullValue =
        if (value is KSVerifiable<*>) value.value == null && !value.isUndefined()
        else value == null

    val isNotNullableAndNull = !nullable && isNullValue

    if (isNotNullableAndNull) {
        val messageTemplate = if (message != null) "$message or null" else "can be a null value"

        context?.disableDefaultConstraintViolation()
        context?.buildConstraintViolationWithTemplate(
            messageTemplate,
        )?.addConstraintViolation()
        return false
    }

    return true
}
