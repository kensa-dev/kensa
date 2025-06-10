package dev.kensa.util

import java.lang.System.err
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

typealias ReflectPredicate<T> = (T) -> Boolean

val Class<*>.isKotlinClass get() = declaredAnnotations.any { it.annotationClass.qualifiedName == "kotlin.Metadata" }
val KClass<*>.isKotlinClass get() = java.isKotlinClass
val Class<*>.isKotlinObject get() = isKotlinClass && kotlin.objectInstance != null
val KClass<*>.isKotlinObject get() = isKotlinClass && objectInstance != null

val Method.normalisedPlatformName: String get() = takeIf { declaringClass.isKotlinClass }?.kotlinFunction?.name ?: name

fun KProperty<*>.isPublic(): Boolean = visibility == null || visibility.toString() == "PUBLIC"
fun Field.isStatic(): Boolean = Modifier.isStatic(modifiers)
fun Field.isPublicStatic(): Boolean = Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
inline fun <reified T> Field.hasType(): Boolean = T::class.java.isAssignableFrom(this.type)

inline fun <reified T> KProperty1<*, *>.hasType(): Boolean {
    val type = T::class.createType(arguments = listOf(KTypeProjection.STAR))
    return returnType.isSubtypeOf(type)
}

fun <T> ReflectPredicate<T>.and(other: ReflectPredicate<T>): ReflectPredicate<T> = { it: T -> invoke(it) && other.invoke(it) }

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
internal fun <T> Any.invokeMethodOrNull(method: Method): T? = method.run {
    isAccessible = true
    try {
        invoke(this@invokeMethodOrNull) as T?
    }catch(e: Exception) {
        err.println("Unable to invoke method [${method.name}] on [${this@invokeMethodOrNull::class.simpleName}"); e.printStackTrace(err)
        null
    }
}

fun Class<*>.findMethod(name: String) =
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
    takeUnless { isStatic() }?.run {
        (kotlinProperty?.run {
            valueOfKotlinPropertyIn(target)
        } ?: valueOfJavaFieldIn(target))
    } ?: valueOfJavaStaticField()
}

fun KProperty<*>.valueOfKotlinPropertyIn(target: Any): Any? = run {
    javaField?.takeIf { it.isStatic() }?.run {
        valueOfJavaStaticField()
    } ?: run {
        isAccessible = true
        getter.call(target)?.realValue()
    }
}

inline fun <reified T> Field.valueOfJavaStaticField(): T? = run {
    isAccessible = true
    get(null) as? T
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

fun Class<*>.findTestMethods(predicate: ReflectPredicate<Method>) = findMethodsInHierarchy(predicate)

internal inline fun <reified T : Annotation> KProperty<*>.findKotlinOrJavaAnnotation() = findAnnotation() ?: javaField?.findAnnotation() ?: javaGetter?.findAnnotation<T>()
internal inline fun <reified T : Annotation> KProperty<*>.hasKotlinOrJavaAnnotation() = hasAnnotation<T>() || javaElementHasAnnotation<T>()
internal inline fun <reified T : Annotation> KProperty<*>.javaElementHasAnnotation() = javaField?.findAnnotation<T>() != null || javaGetter?.findAnnotation<T>() != null
inline fun <reified T : Annotation> AnnotatedElement.hasAnnotation() = findAnnotation<T>() != null
inline fun <reified T : Annotation> AnnotatedElement.findAnnotation(): T? = annotations.firstOrNull { it is T } as T?

private fun shouldStop(): (Class<*>) -> Boolean = { it == Any::class.java }

private fun Class<*>?.findField(predicate: ReflectPredicate<Field>): Field? =
    this?.takeUnless(shouldStop())?.run {
        declaredFields.singleOrNull { predicate.invoke(it) }
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

/**
 * Resolves a dot-separated path on the given object.
 */
fun resolvePath(startingValue: Any, path: String?): Any? {
    if (path.isNullOrEmpty()) {
        return startingValue
    }

    val segments = path.split(".")
    var currentValue: Any? = startingValue

    for (segment in segments) {
        currentValue = currentValue?.let { resolveSegment(it, segment) } ?: return null
    }

    return currentValue
}

/**
 * Resolves a single path segment on the given object.
 * The segment can represent a property or method.
 */
private fun resolveSegment(target: Any, segment: String): Any? =
    try {
        if (segment.endsWith("()")) {
            val methodName = segment.removeSuffix("()")
            val method = target::class.java.methods.find { it.name == methodName }
                ?: throw NoSuchMethodException("Method $methodName not found on ${target::class.java.name}")
            method.apply { isAccessible = true }.invoke(target)
        } else {
            val property = target::class.members.find { it.name == segment } as? KProperty<*>
                ?: throw NoSuchFieldException("Property $segment not found on ${target::class.java.name}")
            property.javaField?.apply { isAccessible = true }?.get(target)
                ?: property.javaGetter?.apply { isAccessible = true }?.invoke(target)
        }
    } catch (e: Exception) {
        err.println("Accessor threw an exception: "); e.printStackTrace(err); null
    }
