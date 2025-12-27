package dev.kensa.parse

import dev.kensa.*
import dev.kensa.RenderedHintStrategy.*
import dev.kensa.RenderedValueStrategy.*
import dev.kensa.util.*
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

sealed interface ElementDescriptor {
    val name: String
    val isRenderedValue: Boolean
    val isRenderedValueContainer: Boolean get() = false
    val isHighlight: Boolean
    val isParameterizedTestDescription: Boolean get() = false
    val highlight: Highlight?

    fun resolveValue(target: Any, path: String? = null): Any?

    companion object {
        fun forProperty(property: KProperty<*>): PropertyElementDescriptor = PropertyElementDescriptor(property)

        fun forHintedProperty(property: KProperty<*>, directive: RenderingDirective): HintedPropertyElementDescriptor = HintedPropertyElementDescriptor(property, directive)

        fun forResolveHolder(parentDescriptor: PropertyElementDescriptor, property: KProperty<*>): ResolveHolderElementDescriptor = ResolveHolderElementDescriptor(parentDescriptor, property)

        fun forMethod(method: Method): MethodElementDescriptor = MethodElementDescriptor(method)

        fun forParameter(parameter: Parameter, name: String, index: Int): ParameterElementDescriptor = ParameterElementDescriptor(name, parameter, index)

        private fun KProperty<*>.resolveFrom(target: Any): Any? {
            val actualTarget = javaField?.declaringClass?.kotlin?.objectInstance ?: target
            return try {
                valueOfKotlinPropertyIn(actualTarget)
            } catch (e: Exception) {
                System.err.println("Accessor threw an exception: "); e.printStackTrace(System.err); null
            }
        }

        private fun Method.resolveFrom(target: Any): Any? {
            val actualTarget = declaringClass.kotlin.objectInstance ?: target
            return try {
                actualTarget.invokeMethodOrNull(this)
            } catch (e: Exception) {
                System.err.println("Method accessor threw an exception: "); e.printStackTrace(System.err); null
            }
        }
    }

    class ParameterElementDescriptor(override val name: String, private val parameter: Parameter, private val index: Int) : ElementDescriptor {
        override val isRenderedValue: Boolean = parameter.hasAnnotation<RenderedValue>()
        override val isHighlight: Boolean = parameter.hasAnnotation<Highlight>()
        override val highlight: Highlight? = parameter.findAnnotation<Highlight>()
        override val isParameterizedTestDescription: Boolean by lazy { parameter.hasAnnotation<ParameterizedTestDescription>() }

        override fun resolveValue(target: Any, path: String?): Any? {
            val initialValue = (target as Array<*>)[index] ?: return null

            return resolvePath(initialValue, path)
        }
    }

    class MethodElementDescriptor(private val method: Method) : ElementDescriptor {
        override val name: String = method.name
        override val isRenderedValue: Boolean = method.hasAnnotation<RenderedValue>()
        override val isHighlight: Boolean = method.hasAnnotation<Highlight>()
        override val highlight: Highlight? = method.findAnnotation<Highlight>()

        val hasParameters: Boolean = method.parameters.isNotEmpty()

        override fun resolveValue(target: Any, path: String?): Any? =
            method.resolveFrom(target)?.let { resolvePath(it, path) }
    }

    class PropertyElementDescriptor(private val property: KProperty<*>) : ElementDescriptor {
        override val name: String = property.name
        override val isRenderedValue: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValue>()
        override val isRenderedValueContainer: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValueContainer>()
        override val isHighlight: Boolean = property.hasKotlinOrJavaAnnotation<Highlight>()
        override val highlight: Highlight? = property.findKotlinOrJavaAnnotation<Highlight>()

        override fun resolveValue(target: Any, path: String?): Any? =
            property.resolveFrom(target)?.let { resolvePath(it, path) }
    }

    class HintedPropertyElementDescriptor(
        private val property: KProperty<*>,
        private val directive: RenderingDirective
    ) : ElementDescriptor {
        override val name: String = property.name
        override val isRenderedValue: Boolean = true
        override val isHighlight: Boolean = property.hasKotlinOrJavaAnnotation<Highlight>()
        override val highlight: Highlight? = property.findKotlinOrJavaAnnotation<Highlight>()

        override fun resolveValue(target: Any, path: String?): HintedValue? =
            property.resolveFrom(target)?.let { instance ->
                HintedValue(
                    value = computeValue(instance),
                    hint = computeHint(instance)
                )
            }

        private fun computeValue(instance: Any): Any? = when (directive.valueStrategy) {
            UseIdentifierName -> name
            UseToString -> instance.toString()
            UseMethod -> resolvePath(instance, "${directive.valueParam}()")
            UseProperty -> resolvePath(instance, directive.valueParam)
        }

        private fun computeHint(instance: Any): String? = when (directive.hintStrategy) {
            HintFromProperty -> resolvePath(instance, directive.hintParam)?.toString()
            HintFromMethod -> resolvePath(instance, "${directive.hintParam}()")?.toString()
            NoHint -> null
        }
    }

    class HintedValue(val value: Any?, val hint: String?)

    class ResolveHolderElementDescriptor(private val parentDescriptor: PropertyElementDescriptor, property: KProperty<*>) : ElementDescriptor {
        override val name: String = property.name
        override val isRenderedValue: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValue>()
        override val isRenderedValueContainer: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValueContainer>()
        override val isHighlight: Boolean = property.hasKotlinOrJavaAnnotation<Highlight>()
        override val highlight: Highlight? = property.findKotlinOrJavaAnnotation<Highlight>()

        override fun resolveValue(target: Any, path: String?): Any? {
            val holder = parentDescriptor.resolveValue(target) ?: return null

            return resolvePath(holder, path)
        }
    }
}