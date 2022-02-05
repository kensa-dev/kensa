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
import kotlin.reflect.jvm.kotlinFunction

val Class<*>.isKotlinClass get() = declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
val KClass<*>.isKotlinClass get() = java.isKotlinClass

val Method.normalisedName: String get() = takeIf { declaringClass.isKotlinClass }?.kotlinFunction?.name ?: name

fun interface MethodPredicate {
    operator fun invoke(method: Method): Boolean

    fun and(other: MethodPredicate): MethodPredicate = MethodPredicate { m -> invoke(m) && other.invoke(m) }
}

private val annotatedAsTests = MethodPredicate { hasAnnotation<Test>(it) || hasAnnotation<ParameterizedTest>(it) }
private val allMethods = MethodPredicate { true }
private fun notDeclaredIn(clazz: Class<*>) = MethodPredicate { it.declaringClass != clazz }
private fun withMethodName(name: String) = MethodPredicate { it.name == name }
private fun noParameters() = MethodPredicate { it.parameters.isEmpty() }
private fun withPropertyName(name: String): (KProperty1<out Any?, Any?>) -> Boolean = { it.name == name }

fun interface FieldPredicate {
    operator fun invoke(field: Field): Boolean
}

private val allFields = FieldPredicate { true }
private val annotatedAsScenario = FieldPredicate { hasAnnotation<Scenario>(it) }
private fun withFieldName(name: String) = FieldPredicate { it.name == name }

internal fun Method.actualDeclaringClass(): Class<*> =
    findMethods(withMethodName(name).and(notDeclaredIn(declaringClass)), declaringClass).firstOrNull()?.run {
        declaringClass
    } ?: declaringClass

@Suppress("UNCHECKED_CAST")
internal fun <T> Any.invokeMethod(name: String): T? {
    findKotlinProperties(withPropertyName(name), this::class.java).firstOrNull()?.run {
        isAccessible = true
        return getter.call(this@invokeMethod) as T?
    }

    findMethods(withMethodName(name).and(noParameters()), this::class.java).firstOrNull()?.run {
        isAccessible = true
        return invoke(this@invokeMethod) as T?
    }

    throw IllegalArgumentException("No method or property [$name] found in class [${this::class}]")

}

@Suppress("UNCHECKED_CAST")
internal fun <T> Any.invokeMethod(method: Method): T? = method.run {
    isAccessible = true
    invoke(this@invokeMethod) as T?
}

internal fun Class<*>.findMethod(name: String) =
    findMethods(withMethodName(name), this)
        .firstOrNull()
        ?: throw IllegalArgumentException("No method [$name] found in class [${this}]")

internal fun Class<*>.findRequiredField(name: String) = findField(withFieldName(name), this) ?: throw IllegalArgumentException("Did not find field [$name] in class [$simpleName]")

internal fun <T> Any.fieldValue(name: String): T? = this::class.java.findRequiredField(name).valueOfIn(this)

internal fun Class<*>.allMethods() = findMethods(allMethods, this)

internal fun Class<*>.allFields() = findFields(allFields, this)

private fun findFields(predicate: FieldPredicate, clazz: Class<*>, results: MutableSet<Field> = LinkedHashSet()): Set<Field> =
    results.also {
        clazz.takeUnless { it == Any::class.java }?.apply {
            it.addAll(clazz.declaredFields.filter { predicate.invoke(it) })
            findFields(predicate, clazz.superclass, it)
        }
    }

@Suppress("UNCHECKED_CAST")
internal fun <T> Field.valueOfIn(target: Any): T? = run {
    isAccessible = true
    get(target)?.let {
        when (it) {
            is Function0<*> -> it() as T?
            is Supplier<*> -> it.get() as T?
            else -> it as T?
        }
    }
}

internal fun Class<*>.testMethods() = findMethods(annotatedAsTests, this)

internal inline fun <reified T : Annotation> findAnnotation(element: AnnotatedElement): T? = element.annotations?.firstOrNull { it is T } as T?

internal inline fun <reified T : Annotation> hasAnnotation(element: AnnotatedElement): Boolean = findAnnotation<T>(element) != null

internal fun Any.scenarioAccessor(): CachingScenarioMethodAccessor =
    CachingScenarioMethodAccessor(this, findFields(annotatedAsScenario, this::class.java).map { it.name }.toSet())

private fun findField(predicate: FieldPredicate, clazz: Class<*>?): Field? =
    clazz?.takeUnless { it == Any::class.java }?.run {
        declaredFields.singleOrNull { predicate.invoke(it) } ?: findField(predicate, clazz.superclass)
    }

private fun findKotlinProperties(
    predicate: (KProperty1<out Any?, Any?>) -> Boolean,
    clazz: Class<*>?,
    results: MutableSet<KProperty1<out Any?, Any?>> = LinkedHashSet()
): Set<KProperty1<out Any?, Any?>> =
    results.also {
        clazz?.takeUnless { it == Any::class.java }?.apply {
            if (isKotlinClass) kotlin.declaredMemberProperties.filterTo(it, predicate)
            findKotlinProperties(predicate, superclass, it)
        }
    }

private fun findMethods(predicate: MethodPredicate, clazz: Class<*>?, results: MutableSet<Method> = LinkedHashSet()): Set<Method> =
    results.also {
        clazz?.takeUnless { it == Any::class.java }?.apply {
            declaredMethods.filterTo(it) { predicate.invoke(it) }
            findMethods(predicate, superclass, it)
            interfaces.forEach { i -> findMethods(predicate, i, it) }
        }
    }