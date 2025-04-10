package dev.kensa.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

typealias ReflectPredicate<T> = (T) -> Boolean

val Class<*>.isKotlinClass get() = declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
val KClass<*>.isKotlinClass get() = java.isKotlinClass

val Method.normalisedPlatformName: String get() = takeIf { declaringClass.isKotlinClass }?.kotlinFunction?.name ?: name

fun <T> ReflectPredicate<T>.and(other: ReflectPredicate<T>): ReflectPredicate<T> = { it: T -> invoke(it) && other.invoke(it) }

private val annotatedAsTests: ReflectPredicate<Method> = { it -> it.hasAnnotation<Test>() || it.hasAnnotation<ParameterizedTest>() }
private val allTheMethods: ReflectPredicate<Method> = { true }
private fun notDeclaredIn(clazz: Class<*>): ReflectPredicate<Method> = { it.declaringClass != clazz }
private fun named(name: String): ReflectPredicate<Method> = { it.name == name }
private fun noParameters(): ReflectPredicate<Method> = { it.parameters.isEmpty() }
private val allTheFields: ReflectPredicate<Field> = { true }
private fun withFieldName(name: String): ReflectPredicate<Field> = { it.name == name }
private fun withName(name: String): (KProperty1<out Any?, Any?>) -> Boolean = { it.name == name }

internal fun Method.actualDeclaringClass(): Class<*> {

    fun Method.findMatchingMethodsInHierarchy() = declaringClass.findMethodsInHierarchy(named(name).and(notDeclaredIn(declaringClass))).firstOrNull()

    return findMatchingMethodsInHierarchy()?.declaringClass ?: declaringClass
}

@Suppress("UNCHECKED_CAST")
internal fun <T> Any.invokeMethod(name: String): T? {

    this::class.java.findKotlinPropertiesInHierarchy(withName(name)).firstOrNull()?.let { property ->
        property.isAccessible = true
        return property.getter.call(this) as T?
    }

    this::class.java.findMethodsInHierarchy(named(name).and(noParameters())).firstOrNull()?.let { method ->
        method.isAccessible = true
        return method.invoke(this) as T?
    }

    throw IllegalArgumentException("No method or property [$name] found in class [${this::class}]")
}

@Suppress("UNCHECKED_CAST")
internal fun <T> Any.invokeMethod(method: Method): T? = method.run {
    isAccessible = true
    invoke(this@invokeMethod) as T?
}

internal fun Class<*>.findMethod(name: String) =
    findMethodsInHierarchy(named(name))
        .firstOrNull()
        ?: throw IllegalArgumentException("No method [$name] found in class [${this}]")

internal fun Class<*>.findRequiredField(name: String) = this.findField(withFieldName(name)) ?: throw IllegalArgumentException("Did not find field [$name] in class [$simpleName]")

internal fun Any.fieldValue(name: String): Any? = this::class.java.findRequiredField(name).valueOfIn(this)

internal val Class<*>.allMethods get() = findMethodsInHierarchy(allTheMethods)
internal val Class<*>.allProperties: Set<KProperty<*>> get() = kotlin.allProperties
internal val KClass<*>.allProperties: Set<KProperty<*>> get() = memberProperties.toSet() + findStaticPropertiesInHierarchy()
internal val Class<*>.allFields get() = findFieldsInHierarchy(allTheFields)

@Suppress("UNCHECKED_CAST")
private fun Field.valueOfIn(target: Any): Any? = run {
    takeUnless { Modifier.isStatic(modifiers) }?.run {
        (kotlinProperty?.run {
            valueOfKotlinPropertyIn(target)
        } ?: valueOfJavaFieldIn(target))
    } ?: valueOfJavaStaticFieldIn(target)
}

fun KProperty<*>.valueOfKotlinPropertyIn(target: Any): Any? = run {
    javaField?.takeIf { Modifier.isStatic(it.modifiers) }?.run {
        valueOfJavaStaticFieldIn(target)
    } ?: run {
        isAccessible = true
        getter.call(target)?.realValue()
    }
}

private fun Field.valueOfJavaStaticFieldIn(target: Any): Any? = kotlin.run {
    isAccessible = true
    get(target::class.java)
}

private fun Field.valueOfJavaFieldIn(target: Any) = run {
    isAccessible = true
    get(target)?.realValue()
}

private fun Any.realValue() =
    when (this) {
        is Function0<*> -> this()
        is Supplier<*> -> this.get()
        else -> this
    }

internal fun Class<*>.findTestMethods() = findMethodsInHierarchy(annotatedAsTests)

internal inline fun <reified T : Annotation> KProperty<*>.findKotlinOrJavaAnnotation() = findAnnotation() ?: javaField?.findAnnotation() ?: javaGetter?.findAnnotation<T>()
internal inline fun <reified T : Annotation> KProperty<*>.hasKotlinOrJavaAnnotation() = hasAnnotation<T>() || javaElementHasAnnotation<T>()
internal inline fun <reified T : Annotation> KProperty<*>.javaElementHasAnnotation() = javaField?.findAnnotation<T>() != null || javaGetter?.findAnnotation<T>() != null
internal inline fun <reified T : Annotation> AnnotatedElement.hasAnnotation() = findAnnotation<T>() != null
internal inline fun <reified T : Annotation> AnnotatedElement.findAnnotation(): T? = annotations?.firstOrNull { it is T } as T?

private fun shouldStop(): (Class<*>) -> Boolean = { it == Any::class.java }

private fun Class<*>?.findField(predicate: ReflectPredicate<Field>): Field? =
    this?.takeUnless(shouldStop())?.run {
        this.declaredFields.singleOrNull { predicate.invoke(it) }
            ?: superclass.findField(predicate)
    }

private fun Class<*>.findKotlinPropertiesInHierarchy(predicate: (KProperty1<out Any?, Any?>) -> Boolean): Set<KProperty1<out Any?, Any?>> {
    val results = mutableSetOf<KProperty1<out Any?, Any?>>()

    fun Class<*>.traverseClassHierarchy() {
        takeUnless(shouldStop())?.apply {
            if (isKotlinClass) kotlin.declaredMemberProperties.filterTo(results, predicate)
            superclass.findKotlinPropertiesInHierarchy(predicate)
        }
    }

    traverseClassHierarchy()

    return results
}

private fun Class<*>.findFieldsInHierarchy(predicate: ReflectPredicate<Field>): Set<Field> {
    val results = mutableSetOf<Field>()

    fun Class<*>.traverseClassHierarchy() {
        takeUnless(shouldStop())?.apply {
            results.addAll(declaredFields.filter { predicate.invoke(it) })
            superclass?.traverseClassHierarchy()
        }
    }

    traverseClassHierarchy()

    return results
}

private fun Class<*>.findMethodsInHierarchy(predicate: ReflectPredicate<Method>): Set<Method> {
    val results = mutableSetOf<Method>()

    fun Class<*>.traverseClassHierarchy() {
        takeUnless(shouldStop())?.apply {
            declaredMethods.filterTo(results, predicate)
            superclass?.traverseClassHierarchy()
            interfaces.forEach { it.traverseClassHierarchy() }
        }
    }

    traverseClassHierarchy()

    return results
}

private fun KClass<*>.findStaticPropertiesInHierarchy(): Set<KProperty<*>> {
    val results = mutableSetOf<KProperty<*>>()

    fun KClass<*>.traverseClassHierarchy() {
        takeUnless { it == Any::class }?.apply {
            results.addAll(staticProperties)
            superclasses.forEach(KClass<*>::traverseClassHierarchy)
        }
    }

    traverseClassHierarchy()

    return results
}
