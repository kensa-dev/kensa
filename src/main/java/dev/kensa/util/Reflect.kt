package dev.kensa.util

import dev.kensa.Scenario
import dev.kensa.parse.CachingScenarioMethodAccessor
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.lang.reflect.Parameter
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

object Reflect {

    fun isKotlinClass(target: KClass<*>): Boolean = target.java.declaredAnnotations.any {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
    }

    fun isJavaClass(target: KClass<*>): Boolean = target.java.declaredAnnotations.none {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
    }

    fun <T> propertyValue(name: String, target: Any): T? {
        val property = findProperty(name, target::class) ?: throw IllegalArgumentException("No property [$name] found in class [${target::class}]")

        return propertyValue<T>(property, target)?.let {
            @Suppress("UNCHECKED_CAST")
            when (it) {
                is Function0<*> -> it() as T?
                is Supplier<*> -> it.get() as T?
                else -> it
            }
        }
    }

    fun <T> propertyValue(property: KProperty1<out Any?, Any?>, target: Any): T? = property.run {
        isAccessible = true
        @Suppress("UNCHECKED_CAST")
        getter.call(target) as T
    }

    fun propertiesOf(kClass: KClass<*>): List<KProperty1<out Any?, Any?>> = _propertiesOf(kClass, ArrayList())

    private fun findProperty(name: String, kClass: KClass<*>?): KProperty1<out Any?, Any?>? =
            if (kClass == null || kClass == Any::class) null else {
                kClass.declaredMemberProperties.singleOrNull { it.name == name } ?: findProperty(name, kClass.superclasses.singleOrNull { !it.java.isInterface })
            }

    private fun _propertiesOf(kClass: KClass<*>?, properties: MutableList<KProperty1<out Any?, Any?>>): List<KProperty1<out Any?, Any?>> =
            if (kClass == null || kClass == Any::class)
                properties
            else properties.apply {
                properties.addAll(kClass.declaredMemberProperties)
                _propertiesOf(kClass.superclasses.singleOrNull { !it.java.isInterface }, this)
            }

    fun <T> invoke(name: String, target: Any): T? {
        val function = findFunctions(target::class, LinkedHashSet()) { it.name == name && it.valueParameters.isEmpty() }.firstOrNull()
                ?: throw IllegalArgumentException("No function [$name] found in class [${target::class}]")

        function.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return function.call(target) as T?
    }

    fun testFunctionsOf(target: KClass<*>): Set<KFunction<*>> = findFunctions(target, LinkedHashSet()) { it.findAnnotation<Test>() != null || it.findAnnotation<ParameterizedTest>() != null }

    private fun findFunctions(kClass: KClass<*>?, results: MutableSet<KFunction<*>>, predicate: (KFunction<*>) -> Boolean): Set<KFunction<*>> {
        return if (kClass == null || kClass == Any::class) results else results.apply {
            kClass.declaredFunctions.filterTo(this, predicate)

            kClass.superclasses.forEach {
                findFunctions(it, this, predicate)
            }
        }
    }

    inline fun <reified T : Annotation> findAnnotation(property: KProperty1<out Any?, Any?>): T? = property.javaField?.annotations?.firstOrNull { it is T } as T?

    inline fun <reified T : Annotation> hasAnnotation(property: KProperty1<out Any?, Any?>): Boolean = findAnnotation<T>(property) != null

    inline fun <reified T : Annotation> findAnnotation(parameter: Parameter): T? = parameter.annotations?.firstOrNull { it is T } as T?

    inline fun <reified T : Annotation> hasAnnotation(parameter: Parameter): Boolean = findAnnotation<T>(parameter) != null

    fun scenarioAccessorFor(target: Any): CachingScenarioMethodAccessor {
        val names: MutableSet<String> = HashSet()
        var kClass: KClass<*>? = target::class
        while (kClass != null && kClass != Any::class) {
            kClass.declaredMemberProperties
                    .filter { property -> hasAnnotation<Scenario>(property) }
                    .forEach { property -> names.add(property.name) }
            kClass = kClass.superclasses.singleOrNull { !it.java.isInterface }
        }
        return CachingScenarioMethodAccessor(target, names)
    }
}