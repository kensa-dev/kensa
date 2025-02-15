package dev.kensa.util

import dev.kensa.Scenario
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

val Class<*>.isKotlinClass get() = declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
val KClass<*>.isKotlinClass get() = java.isKotlinClass

val Method.normalisedPlatformName: String get() = takeIf { declaringClass.isKotlinClass }?.kotlinFunction?.name ?: name

fun interface ReflectPredicate<T> {
    operator fun invoke(target: T): Boolean

    fun and(other: ReflectPredicate<T>): ReflectPredicate<T> = ReflectPredicate { m -> invoke(m) && other.invoke(m) }
}

private val annotatedAsTests = ReflectPredicate<Method> { it.hasAnnotation<Test>() || it.hasAnnotation<ParameterizedTest>() }
private val allTheMethods = ReflectPredicate<Method> { true }
private fun notDeclaredIn(clazz: Class<*>) = ReflectPredicate<Method> { it.declaringClass != clazz }
private fun withMethodName(name: String) = ReflectPredicate<Method> { it.name == name }
private fun noParameters() = ReflectPredicate<Method> { it.parameters.isEmpty() }
private fun withPropertyName(name: String): (KProperty1<out Any?, Any?>) -> Boolean = { it.name == name }

private val allTheFields = ReflectPredicate<Field> { true }
private val annotatedAsScenario = ReflectPredicate<Field> { it.hasAnnotation<Scenario>() }
private fun withFieldName(name: String) = ReflectPredicate<Field> { it.name == name }

internal val Method.actualDeclaringClass: Class<*>
    get() = findMethods(withMethodName(name).and(notDeclaredIn(declaringClass)), declaringClass).firstOrNull()?.run {
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

internal fun Any.fieldValue(name: String): Any? = this::class.java.findRequiredField(name).valueOfIn(this)

internal val Class<*>.allMethods get() = findMethods(allTheMethods, this)
internal val Class<*>.allProperties: Collection<KProperty<*>> get() = kotlin.allProperties
internal val KClass<*>.allProperties: Collection<KProperty<*>> get() = memberProperties + allStaticProperties()
internal val Class<*>.allFields get() = findFields(allTheFields, this)

private fun KClass<*>.allStaticProperties(results: MutableSet<KProperty<*>> = LinkedHashSet()): Collection<KProperty<*>> =
    results.also { r ->
        takeUnless { it == Any::class }?.apply {
            r.addAll(staticProperties)
            superclasses.forEach { sc ->
                sc.allStaticProperties(r)
            }
        }
    }

private fun findFields(predicate: ReflectPredicate<Field>, clazz: Class<*>, results: MutableSet<Field> = LinkedHashSet()): Set<Field> =
    results.also {
        clazz.takeUnless { it == Any::class.java }?.apply {
            it.addAll(declaredFields.filter { predicate.invoke(it) })
            superclass?.let { sc -> findFields(predicate, sc, it) }
        }
    }

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

internal fun Class<*>.testMethods() = findMethods(annotatedAsTests, this)

internal inline fun <reified T : Annotation> KProperty<*>.findKotlinOrJavaAnnotation() = findAnnotation() ?: javaField?.findAnnotation() ?: javaGetter?.findAnnotation<T>()
internal inline fun <reified T : Annotation> KProperty<*>.hasKotlinOrJavaAnnotation() = hasAnnotation<T>() || javaElementHasAnnotation<T>()
internal inline fun <reified T : Annotation> KProperty<*>.javaElementHasAnnotation() = javaField?.findAnnotation<T>() != null || javaGetter?.findAnnotation<T>() != null
internal inline fun <reified T : Annotation> AnnotatedElement.hasAnnotation() = findAnnotation<T>() != null
internal inline fun <reified T : Annotation> AnnotatedElement.findAnnotation(): T? = annotations?.firstOrNull { it is T } as T?

private fun findField(predicate: ReflectPredicate<Field>, clazz: Class<*>?): Field? =
    clazz?.takeUnless { it == Any::class.java }?.run {
        declaredFields.singleOrNull { predicate.invoke(it) }
            ?: findField(predicate, clazz.superclass)
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

private fun findMethods(predicate: ReflectPredicate<Method>, clazz: Class<*>?, results: MutableSet<Method> = LinkedHashSet()): Set<Method> =
    results.also {
        clazz?.takeUnless { it == Any::class.java }?.apply {
            declaredMethods.filterTo(it) { predicate.invoke(it) }
            findMethods(predicate, superclass, it)
            interfaces.forEach { i -> findMethods(predicate, i, it) }
        }
    }