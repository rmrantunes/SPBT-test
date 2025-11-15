package co.bondspot.spbttest.springweb.util.validation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder

/**
 * To validate to REST API user/consumer the possible missing fields.
 *
 * Implementation for Kotlin Serialization and Spring Validation.
 */
@Serializable
class KSVerifiable<T>(
    val value: T? = null,
    private var isUndefined: Boolean = true,

    /**
     * When deserializing, Kotlin Serialization has its own validation and throws its own exceptions
     * before Spring Validation execution for field types.
     *
     * This flag property is needed in order to keep track on those failed fields and return the
     * validation messages for type mismatch to the REST API user/consumer.
     */
    val hasDeserializationTypeException: Boolean = false,
) {
    init {
        if (value != null) isUndefined = false
    }

    fun isUndefined(): Boolean = isUndefined

    /**
     * Make sure if there's no error messages from Spring Validation before executing
     */
    @Suppress("UNCHECKED_CAST")
    fun dangerouslyForceCast(): T {
        return value as T
    }
}

open class KSVerifiableSerializer<T>(private val tSerializer: KSerializer<T>) :
    KSerializer<KSVerifiable<T>> {
    override val descriptor: SerialDescriptor = tSerializer.descriptor

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: KSVerifiable<T>) {
        if (value.isUndefined()) {
            encoder.encodeNull()
        } else {
            encoder.encodeNullableSerializableValue(tSerializer, value.value)
        }
    }

    override fun deserialize(decoder: Decoder): KSVerifiable<T> {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("This serializer only works with JSON")
        val element = jsonDecoder.decodeJsonElement()
        return try {
            val v = jsonDecoder.json.decodeFromJsonElement(tSerializer, element)
            KSVerifiable(v, isUndefined = false)
        } catch (_: Exception) {
            KSVerifiable(hasDeserializationTypeException = true)
        }
    }
}

object KSVString : KSVerifiableSerializer<String>(String.serializer())

object KSVInt : KSVerifiableSerializer<Int>(Int.serializer())

// object KSVBigDecimal : KSVerifiableSerializer<BigDecimal>(BigDecimalSerializer)
