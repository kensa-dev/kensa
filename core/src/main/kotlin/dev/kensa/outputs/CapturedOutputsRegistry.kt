package dev.kensa.outputs

import dev.kensa.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * A registry for outputs.
 * Stores outputs by their variable names and allows looking them up.
 */
object CapturedOutputsRegistry {
    private val lock = Any()
    private val outputsByName = mutableMapOf<String, CapturedOutput<*>>()
    private val outputsByKey = mutableMapOf<String, CapturedOutput<*>>()

    internal fun clearOutputs() {
        synchronized(this) {
            outputsByName.clear()
            outputsByKey.clear()
        }
    }

    /**
     * Looks up an output by its variable name.
     *
     * @param name The variable name of the output
     * @return The output, or null if not found
     * @throws IllegalArgumentException if no output with the given variable name is registered
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> lookupCapturedOutput(name: String): CapturedOutput<T> = outputsByName[name] as? CapturedOutput<T> ?: error("No output with name [$name] registered")

    @JvmStatic
    fun registerCapturedOutputs(vararg containers: CapturedOutputContainer) = containers.forEach { register(it.javaClass) }

    @JvmStatic
    @SafeVarargs
    fun registerCapturedOutputs(vararg containers: Class<out CapturedOutputContainer>) = containers.forEach { register(it) }

    internal fun register(containerClass: Class<out CapturedOutputContainer>) {
        synchronized(lock) {
            if (containerClass.isKotlinClass) {
                val kClass = containerClass.kotlin
                if (kClass.isKotlinObject) {
                    kClass.registerCapturedOutputProperties()
                }
            } else {
                containerClass.registerCapturedOutputFields()
            }
        }
    }

    private fun KClass<out CapturedOutputContainer>.registerCapturedOutputProperties() {
        memberProperties.forEach { property ->
            if (property.hasType<CapturedOutput<*>>()) {
                val newCapturedOutput = with(property) {
                    if (isPublic()) {
                        getter.call(objectInstance) as CapturedOutput<*>
                    } else if(isCompanion) {
                        isAccessible = true
                        getter.call(objectInstance) as CapturedOutput<*>
                    } else {
                        java.getDeclaredField(property.name)
                            .apply { isAccessible = true }
                            .get(objectInstance)
                    }
                } as CapturedOutput<*>

                newCapturedOutput.register(property.name)
            }

        }
    }

    private fun Class<out CapturedOutputContainer>.registerCapturedOutputFields() {
        for (field in this.declaredFields) {
            if (field.isStatic() && field.hasType<CapturedOutput<*>>()) {
                val newCapturedOutput = field.valueOfJavaStaticField<CapturedOutput<*>>() ?: error("CapturedOutput field [${field.declaringClass.simpleName}.${field.name}] contains a null value")
                newCapturedOutput.register(field.name)

            }
        }
    }

    private fun CapturedOutput<*>.register(name: String) {
        // Ensure this name is not already registered for a different key
        outputsByName[name]?.also { existingCapturedOutput ->
            if (existingCapturedOutput === this || existingCapturedOutput.key == this.key) {
                return
            }
            throw IllegalStateException("Duplicate output (field/property) name: ${name}. AN output with this name is already registered with key: ${existingCapturedOutput.key}")
        }

        // Ensure an output with the same key is not already registered
        outputsByKey[this.key]?.also { existingCapturedOutput ->
            if (existingCapturedOutput === this) {
                // Register the new output by name
                outputsByName[name] = this
                return
            }
            throw IllegalStateException("Duplicate output key: ${this.key}. AN output with this key is already registered with name: ${outputsByName.entries.find { it.value.key == key }?.key}")
        }

        outputsByName[name] = this
        outputsByKey[key] = this
    }
}
