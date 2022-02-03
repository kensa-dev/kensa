package dev.kensa.util

import dev.kensa.Scenario
import dev.kensa.parse.CachingScenarioMethodAccessor
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*
import java.util.function.Supplier
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

object Reflect {

    fun isKotlinClass(target: KClass<*>): Boolean = target.java.declaredAnnotations.any {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
    }

    fun isJavaClass(target: KClass<*>): Boolean = target.java.declaredAnnotations.none {
        it.annotationClass.qualifiedName == "kotlin.Metadata"
    }

    // Only need parameterless method invocation
    fun <T> invoke(name: String, target: Any): T? {
        // Try to find a kotlin property first...
        return if (isKotlinClass(target::class) && isProperty(name, target::class)) {
            propertyValue(findProperty(name, target::class)!!, target)
        } else invokeMethod<T>(name, target)
    }

    private fun isProperty(name: String, target: KClass<*>): Boolean {
        return findProperty(name, target) != null
    }

    private fun findProperty(name: String, kClass: KClass<*>?): KProperty1<out Any?, Any?>? =
        if (kClass == null || kClass == Any::class) null else {
            kClass.declaredMemberProperties.singleOrNull { it.name == name }
                ?: findProperty(name, kClass.superclasses.singleOrNull { !it.java.isInterface })
        }

    fun <T> propertyValue(property: KProperty1<out Any?, Any?>, target: Any): T? = property.run {
        isAccessible = true
        @Suppress("UNCHECKED_CAST")
        getter.call(target) as T
    }

    fun findMethod(name: String, target: KClass<*>): Method {
        return findMethods(target.java, LinkedHashSet()) { it.name == name }
            .firstOrNull()
            ?: throw IllegalArgumentException("No method [$name] found in class [${target::class}]")
    }

    private fun <T> invokeMethod(name: String, target: Any): T? {
        val method = findMethods(target::class.java, LinkedHashSet()) { it.name == name && it.parameters.isEmpty() }
            .firstOrNull()
            ?: throw IllegalArgumentException("No method [$name] found in class [${target::class}]")

        return invoke<T>(method, target)
    }

    fun <T> invoke(method: Method, target: Any): T? {
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(target) as T?
    }

    private fun findMethods(clazz: Class<*>?, results: MutableSet<Method>, predicate: (Method) -> Boolean): Set<Method> {
        return if (clazz == null || clazz == Any::class.java) results else results.apply {
            clazz.declaredMethods.filterTo(this, predicate)

            findMethods(clazz.superclass, results, predicate)
            clazz.interfaces.forEach {
                findMethods(it, results, predicate)
            }
        }
    }

    fun <T> fieldValue(name: String, target: Any): T? {
        val field = findField(name, target::class.java)
            ?: throw IllegalArgumentException("No field [$name] found in class [${target::class}]")

        return fieldValue(field, target)
    }

    fun findField(name: String, clazz: KClass<*>) : Field? = findField(name, clazz.java)

    fun findField(name: String, clazz: Class<*>): Field? =
        if (clazz == Any::class.java) null else {
            clazz.declaredFields.singleOrNull { it.name == name } ?: findField(name, clazz.superclass)
        }


    fun <T> fieldValue(field: Field, target: Any): T? = field.run {
        isAccessible = true
        get(target)?.let {
            @Suppress("UNCHECKED_CAST")
            when (it) {
                is Function0<*> -> it() as T?
                is Supplier<*> -> it.get() as T?
                else -> it as T?
            }
        }
    }

    fun methodsOf(clazz: Class<*>) : List<Method> = methodsOf(clazz, ArrayList())

    fun fieldsOf(clazz: Class<*>): List<Field> = fieldsOf(clazz, ArrayList())

    private fun methodsOf(clazz: Class<*>, results: MutableList<Method>): List<Method> =
            if (Any::class.java == clazz) {
                results
            } else results.apply {
                addAll(clazz.declaredMethods)
                methodsOf(clazz.superclass, results)
            }

    fun fieldsOf(clazz: KClass<*>): List<Field> = fieldsOf(clazz, ArrayList())

    private fun fieldsOf(clazz: KClass<*>, results: MutableList<Field>): List<Field> =
        results.apply {
            clazz.declaredMemberProperties
                .mapNotNull { it.javaField }
                .forEach {
//                    println(it.name)
                    add(it)
                }
        }

    private fun fieldsOf(clazz: Class<*>, results: MutableList<Field>): List<Field> =
        if (Any::class.java == clazz) {
            results
        } else results.apply {
            addAll(clazz.declaredFields)
            fieldsOf(clazz.superclass, results)
        }

    fun testFunctionsOf(clazz: Class<*>): Set<Method> = findMethods(clazz, LinkedHashSet()) { hasAnnotation<Test>(it) || hasAnnotation<ParameterizedTest>(it) }

    inline fun <reified T : Annotation> findAnnotation(element: AnnotatedElement): T? = element.annotations?.firstOrNull { it is T } as T?

    inline fun <reified T : Annotation> hasAnnotation(element: AnnotatedElement): Boolean = findAnnotation<T>(element) != null

    fun scenarioAccessorFor(target: Any): CachingScenarioMethodAccessor {
        val names: MutableSet<String> = HashSet()
        var clazz: Class<*> = target::class.java
        while (clazz != Any::class.java) {
            clazz.declaredFields
                .filter { field -> hasAnnotation<Scenario>(field) }
                .forEach { field -> names.add(field.name) }
            clazz = clazz.superclass
        }
        return CachingScenarioMethodAccessor(target, names)
    }
}