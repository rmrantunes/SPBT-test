package co.bondspot.spbttest.testutils

import org.instancio.Select
import org.instancio.TargetSelector
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

/**
 * @see
 * https://www.instancio.org/user-guide/#kotlin-method-reference-selector
 * */
class KSelect {
    companion object {
        fun <T, V> field(property: KProperty1<T, V>): TargetSelector {
            val field = property.javaField!!
            return Select.field(field.declaringClass, field.name)
        }
    }
}