package dev.kensa.parse

import dev.kensa.Highlight
import dev.kensa.ParameterizedTestDescription
import dev.kensa.RenderedValue
import dev.kensa.RenderedValueContainer
import dev.kensa.util.findAnnotation
import dev.kensa.util.findKotlinOrJavaAnnotation
import dev.kensa.util.hasAnnotation
import dev.kensa.util.hasKotlinOrJavaAnnotation
import dev.kensa.util.invokeMethodOrNull
import dev.kensa.util.resolvePath
import dev.kensa.util.valueOfKotlinPropertyIn
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KProperty

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

        fun forResolveHolder(parentDescriptor: PropertyElementDescriptor, property: KProperty<*>): ResolveHolderElementDescriptor = ResolveHolderElementDescriptor(parentDescriptor, property)

        fun forMethod(method: Method): MethodElementDescriptor = MethodElementDescriptor(method)

        fun forParameter(parameter: Parameter, name: String, index: Int): ParameterElementDescriptor = ParameterElementDescriptor(name, parameter, index)

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

        override fun resolveValue(target: Any, path: String?): Any? {
            val initialValue: Any = try {
                target.invokeMethodOrNull(method)
            } catch (e: Exception) {
                System.err.println("Accessor threw an exception: "); e.printStackTrace(System.err); null
            } ?: return null

            return resolvePath(initialValue, path)
        }
    }

    class PropertyElementDescriptor(private val property: KProperty<*>) : ElementDescriptor {
        override val name: String = property.name
        override val isRenderedValue: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValue>()
        override val isRenderedValueContainer: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValueContainer>()
        override val isHighlight: Boolean = property.hasKotlinOrJavaAnnotation<Highlight>()
        override val highlight: Highlight? = property.findKotlinOrJavaAnnotation<Highlight>()

        override fun resolveValue(target: Any, path: String?): Any? {
            val initialValue = try {
                property.valueOfKotlinPropertyIn(target)
            } catch (e: Exception) {
                System.err.println("Accessor threw an exception: "); e.printStackTrace(System.err); null
            } ?: return null

            return resolvePath(initialValue, path)
        }
    }

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