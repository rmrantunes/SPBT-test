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

class IsStringValidator : ConstraintValidator<IsString, Any?>, ConstraintValidatorWithDefaults() {
    override fun initialize(constraintAnnotation: IsString?) {
        super.init(
            constraintAnnotation?.message,
            constraintAnnotation?.nullable,
            constraintAnnotation?.required
        )
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        return defaultChecks(value, context) {
            val v = if (value is KSVerifiable<*>) value.value else value
            v is String
        }
    }
}

open class ConstraintValidatorWithDefaults {
    protected var nullable = false
    protected var required = true
    protected var message: String? = null

    fun init(message: String? = null, nullable: Boolean? = false, required: Boolean? = false) {
        this.nullable = nullable == true
        this.required = required == true
        this.message = message
    }

    fun defaultChecks(value: Any?, context: ConstraintValidatorContext?, customCheck: () -> Boolean): Boolean {
        if (!checkHasDeserializationTypeException(value, context, nullable, message)) return false

        if (canBypassRequired(value, required)) return true

        if (!checkRequired(value, context, required, nullable, message)) return false

        if (!checkNullable(value, context, nullable, message)) return false

        return customCheck()
    }

    private fun canBypassRequired(value: Any?, required: Boolean): Boolean {
        return !required && value is KSVerifiable<*> && value.isUndefined()
    }

    private fun checkHasDeserializationTypeException(value: Any?, context: ConstraintValidatorContext?, nullable: Boolean, message: String? = null): Boolean {
        val valid = !(value as KSVerifiable<*>).hasDeserializationTypeException

        if(!valid) {
            val messageTemplate = if (message != null) if (nullable) "$message or null" else message else "value with invalid type (check the docs)"

            context?.disableDefaultConstraintViolation()
            context?.buildConstraintViolationWithTemplate(
                messageTemplate,
            )?.addConstraintViolation()
        }

        return valid
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

}
