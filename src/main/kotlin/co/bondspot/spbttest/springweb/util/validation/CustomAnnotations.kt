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

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [IsOneOfValidator::class])
annotation class IsOneOf(
    val list: Array<String> = [],
    val message: String = "must be one of: {commaList}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val nullable: Boolean = false,
    val required: Boolean = true,
)

class IsOneOfValidator : ConstraintValidator<IsOneOf, Any?>, ConstraintValidatorWithDefaults() {
    lateinit var list: Array<String>
    override fun initialize(constraintAnnotation: IsOneOf?) {
        list = constraintAnnotation?.list!!

        val joinedOptions = list.joinToString(separator = ", ")
        val message = constraintAnnotation.message.replace("{commaList}", joinedOptions)

        super.init(
            message,
            constraintAnnotation.nullable,
            constraintAnnotation.required
        )
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        return defaultChecks(value, context) {
            val v = if (value is KSVerifiable<*>) value.value else value
            val valid = v is String && list.contains(v)

            if (!valid) {
                context?.disableDefaultConstraintViolation()
                context?.buildConstraintViolationWithTemplate(message)?.addConstraintViolation()
            }

            valid
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
        checkHasDeserializationTypeException(value, nullable, message).let { message ->
            if (message.isNotEmpty()) {
                context?.disableDefaultConstraintViolation()
                context?.buildConstraintViolationWithTemplate(message)?.addConstraintViolation()
                return false
            }
        }

        if (canBypassRequired(value, required)) return true

        checkRequired(value, required, nullable, message).let { message ->
            if (message.isNotEmpty()) {
                context?.disableDefaultConstraintViolation()
                context?.buildConstraintViolationWithTemplate(message)?.addConstraintViolation()
                return false
            }
        }

        checkNullable(value, nullable, message).let { message ->
            if (message.isNotEmpty()) {
                context?.disableDefaultConstraintViolation()
                context?.buildConstraintViolationWithTemplate(message)?.addConstraintViolation()
                return false
            }
        }

        return customCheck()
    }

    private fun canBypassRequired(value: Any?, required: Boolean): Boolean {
        return !required && value is KSVerifiable<*> && value.isUndefined()
    }

    private fun checkHasDeserializationTypeException(
        value: Any?,
        nullable: Boolean,
        message: String? = null
    ): String {
        val valid = !(value as KSVerifiable<*>).hasDeserializationTypeException

        if (!valid) {
            return if (message != null) if (nullable) "$message or null" else message else "value with invalid type (check the docs)"
        }

        return ""
    }

    private fun checkRequired(
        value: Any?,
        required: Boolean,
        nullable: Boolean,
        message: String? = null
    ): String {
        if (required && value is KSVerifiable<*> && value.isUndefined()) {
            return if (message != null) if (nullable) "$message or null" else message else "is required"
        }
        return ""
    }

    private fun checkNullable(
        value: Any?,
        nullable: Boolean,
        message: String? = null
    ): String {
        val isNullValue =
            if (value is KSVerifiable<*>) value.value == null && !value.isUndefined()
            else value == null

        val isNotNullableAndNull = !nullable && isNullValue

        if (isNotNullableAndNull)
            return if (message != null) "$message or null" else "can be a null value"

        return ""
    }

}
