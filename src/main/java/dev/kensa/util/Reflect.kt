package dev.kensa.util

import dev.kensa.Scenario
import dev.kensa.parse.CachingScenarioMethodAccessor
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.kotlinFunction

val Class<*>.isKotlinClass get() = declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
val KClass<*>.isKotlinClass get() = java.isKotlinClass
val KClass<*>.isNotKotlinClass get() = !java.isKotlinClass

val Method.normalisedName: String get() = takeIf { declaringClass.isKotlinClass }?.kotlinFunction?.name ?: name

@Suppress("UNCHECKED_CAST")
object Reflect {
    private fun withMethodName(name: String): (Method) -> Boolean = { it.name == name }
    private fun withMethodNameAndNoParameters(name: String): (Method) -> Boolean = { it.name == name && it.parameters.isEmpty() }
    private fun withPropertyName(name: String): (KProperty1<out Any?, Any?>) -> Boolean = { it.name == name }

    fun <T> invokeMethod(name: String, target: Any): T? {
        findKotlinProperties(withPropertyName(name), target::class.java).firstOrNull()?.run {
            isAccessible = true
            return getter.call(target) as T?
        }

        findMethods(withMethodNameAndNoParameters(name), target::class.java).firstOrNull()?.run {
            isAccessible = true
            return invoke(target) as T?
        }

        throw IllegalArgumentException("No method or property [$name] found in class [${target::class}]")
    }

    fun <T> invokeMethod(method: Method, target: Any): T? = method.run {
        isAccessible = true
        invoke(target) as T?
    }

    fun findMethod(name: String, target: KClass<*>) =
        findMethods(withMethodName(name), target.java)
            .firstOrNull()
            ?: throw IllegalArgumentException("No method [$name] found in class [${target::class}]")

    private fun findMethods(predicate: (Method) -> Boolean, clazz: Class<*>?, results: MutableSet<Method> = LinkedHashSet()): Set<Method> =
        results.also {
            clazz?.takeUnless { clazz == Any::class.java }?.apply {
                declaredMethods.filterTo(results, predicate)
                findMethods(predicate, superclass, results)
                interfaces.forEach { i -> findMethods(predicate, i, results) }
            }
        }

    private fun findKotlinProperties(
        predicate: (KProperty1<out Any?, Any?>) -> Boolean,
        clazz: Class<*>?,
        results: MutableSet<KProperty1<out Any?, Any?>> = LinkedHashSet()
    ): Set<KProperty1<out Any?, Any?>> =
        results.also {
            clazz?.takeUnless { clazz == Any::class.java }?.apply {
                if (isKotlinClass) kotlin.declaredMemberProperties.filterTo(results, predicate)
                findKotlinProperties(predicate, superclass, results)
            }
        }

    fun <T> fieldValue(name: String, target: Any): T? {
        val field = findField(name, target::class.java)
            ?: throw IllegalArgumentException("No field [$name] found in class [${target::class}]")

        return fieldValue(field, target)
    }

    fun findRequiredField(name: String, clazz: KClass<*>) = findField(name, clazz.java) ?: throw IllegalArgumentException("Did not find field [$name] in class [${clazz.simpleName}]")

    private fun findField(name: String, clazz: Class<*>): Field? =
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

    fun methodsOf(clazz: Class<*>): List<Method> = methodsOf(clazz, ArrayList())

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

    fun testMethodsOf(clazz: Class<*>): Set<Method> = findMethods({ hasAnnotation<Test>(it) || hasAnnotation<ParameterizedTest>(it) }, clazz)

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