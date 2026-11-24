package dev.kensa.parse

import dev.kensa.*
import dev.kensa.RenderedHintStrategy.*
import dev.kensa.RenderedValueStrategy.*
import dev.kensa.util.*
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

@dev.kensa.KensaInternalApi
sealed interface ElementDescriptor {
    val name: String
    val isRenderedValue: Boolean
    val isExpandableRenderedValue: Boolean get() = false
    val isRenderedValueContainer: Boolean get() = false
    val isHighlight: Boolean
    val isParameterizedTestDescription: Boolean get() = false
    val highlight: Highlight?
    val renderedValueStyle: RenderedValueStyle get() = RenderedValueStyle.Default
    val renderedValueHeaders: List<String> get() = emptyList()
    val containerRenderedMembers: Set<String> get() = emptySet()
    val type: KClass<*>? get() = null

    fun resolveValue(target: Any, path: String? = null): Any?

    companion object {
        fun forProperty(property: KProperty<*>, owner: Any? = null): PropertyElementDescriptor = PropertyElementDescriptor(property, owner)

        fun forHintedProperty(property: KProperty<*>, directive: RenderingDirective, owner: Any? = null): HintedPropertyElementDescriptor = HintedPropertyElementDescriptor(property, directive, owner)

        fun forHintedEnumConstant(enum: Enum<*>, directive: RenderingDirective): HintedEnumConstantElementDescriptor = HintedEnumConstantElementDescriptor(enum, directive)

        fun forResolveHolder(parentDescriptor: PropertyElementDescriptor, property: KProperty<*>): ResolveHolderElementDescriptor = ResolveHolderElementDescriptor(parentDescriptor, property)

        fun forMethod(method: Method, owner: Any? = null): MethodElementDescriptor = MethodElementDescriptor(method, owner)

        fun forParameter(parameter: Parameter, name: String, index: Int): ParameterElementDescriptor = ParameterElementDescriptor(name, parameter, index)

        internal fun Class<*>.renderedMemberNames(): Set<String> =
            allProperties.filter { it.hasKotlinOrJavaAnnotation<RenderedValue>() }.map { it.name }.toSet()

        // A member an object source inherits is declared on a parent with no instance of its own, so the
        // object the descriptor was built from is the receiver. The test instance is one only for its own members.
        private fun Class<*>.receiverFor(owner: Any?, target: Any): Any? =
            owner ?: kotlin.objectInstance ?: target.takeIf { isInstance(it) }

        private fun KProperty<*>.resolveFrom(owner: Any?, target: Any): Any? {
            val declaringClass = javaField?.declaringClass ?: javaGetter?.declaringClass
            val isStatic = javaField?.let { Modifier.isStatic(it.modifiers) } == true
            val actualTarget = if (isStatic || declaringClass == null) owner ?: target else declaringClass.receiverFor(owner, target) ?: return null
            return try {
                valueOfKotlinPropertyIn(actualTarget)
            } catch (e: Exception) {
                System.err.println("Accessor threw an exception: "); e.printStackTrace(System.err); null
            }
        }

        private fun Method.resolveFrom(owner: Any?, target: Any): Any? {
            val actualTarget = if (Modifier.isStatic(modifiers)) owner ?: target else declaringClass.receiverFor(owner, target) ?: return null
            return try {
                actualTarget.invokeMethodOrNull(this)
            } catch (e: Exception) {
                System.err.println("Method accessor threw an exception: "); e.printStackTrace(System.err); null
            }
        }
    }

    class ParameterElementDescriptor(override val name: String, private val parameter: Parameter, val index: Int) : ElementDescriptor {
        override val isRenderedValue: Boolean = parameter.hasAnnotation<RenderedValue>()
        override val isExpandableRenderedValue: Boolean = parameter.hasAnnotation<ExpandableRenderedValue>()
        override val isRenderedValueContainer: Boolean = parameter.hasAnnotation<RenderedValueContainer>()
        override val isHighlight: Boolean = parameter.hasAnnotation<Highlight>()
        override val highlight: Highlight? = parameter.findAnnotation<Highlight>()
        override val isParameterizedTestDescription: Boolean by lazy { parameter.hasAnnotation<ParameterizedTestDescription>() }
        override val renderedValueStyle: RenderedValueStyle = parameter.findAnnotation<ExpandableRenderedValue>()?.renderAs ?: RenderedValueStyle.Default
        override val renderedValueHeaders: List<String> = parameter.findAnnotation<ExpandableRenderedValue>()?.headers?.toList() ?: emptyList()
        override val containerRenderedMembers: Set<String> =
            if (isRenderedValueContainer) parameter.type.renderedMemberNames() else emptySet()

        override fun resolveValue(target: Any, path: String?): Any? {
            val initialValue = (target as Array<*>)[index] ?: return null

            return resolvePath(initialValue, path)
        }
    }

    class MethodElementDescriptor(private val method: Method, private val owner: Any? = null) : ElementDescriptor {
        override val name: String = method.name
        override val isRenderedValue: Boolean = method.hasAnnotation<RenderedValue>()
        override val isExpandableRenderedValue: Boolean = method.hasAnnotation<ExpandableRenderedValue>()
        override val isHighlight: Boolean = method.hasAnnotation<Highlight>()
        override val highlight: Highlight? = method.findAnnotation<Highlight>()
        override val renderedValueStyle: RenderedValueStyle = method.findAnnotation<ExpandableRenderedValue>()?.renderAs ?: RenderedValueStyle.Default
        override val renderedValueHeaders: List<String> = method.findAnnotation<ExpandableRenderedValue>()?.headers?.toList() ?: emptyList()

        val hasParameters: Boolean = method.parameters.isNotEmpty()

        override fun resolveValue(target: Any, path: String?): Any? =
            if (hasParameters) null
            else method.resolveFrom(owner, target)?.let { resolvePath(it, path) }
    }

    class PropertyElementDescriptor(private val property: KProperty<*>, private val owner: Any? = null) : ElementDescriptor {
        override val name: String = property.name
        override val isRenderedValue: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValue>()
        override val isExpandableRenderedValue: Boolean = property.hasKotlinOrJavaAnnotation<ExpandableRenderedValue>()
        override val isRenderedValueContainer: Boolean = property.hasKotlinOrJavaAnnotation<RenderedValueContainer>()
        override val isHighlight: Boolean = property.hasKotlinOrJavaAnnotation<Highlight>()
        override val highlight: Highlight? = property.findKotlinOrJavaAnnotation<Highlight>()
        override val renderedValueStyle: RenderedValueStyle = property.findKotlinOrJavaAnnotation<ExpandableRenderedValue>()?.renderAs ?: RenderedValueStyle.Default
        override val renderedValueHeaders: List<String> = property.findKotlinOrJavaAnnotation<ExpandableRenderedValue>()?.headers?.toList() ?: emptyList()
        override val containerRenderedMembers: Set<String> =
            if (isRenderedValueContainer) (property.returnType.classifier as? KClass<*>)?.java?.renderedMemberNames().orEmpty()
            else emptySet()
        override val type: KClass<*>? = property.returnType.classifier as? KClass<*>

        override fun resolveValue(target: Any, path: String?): Any? =
            property.resolveFrom(owner, target)?.let { resolvePath(it, path) }
    }

    class HintedPropertyElementDescriptor(
        private val property: KProperty<*>,
        private val directive: RenderingDirective,
        private val owner: Any? = null
    ) : ElementDescriptor {
        override val name: String = property.name
        override val isRenderedValue: Boolean = true
        override val isHighlight: Boolean = property.hasKotlinOrJavaAnnotation<Highlight>()
        override val highlight: Highlight? = property.findKotlinOrJavaAnnotation<Highlight>()

        override fun resolveValue(target: Any, path: String?): HintedValue? =
            property.resolveFrom(owner, target)?.let { instance ->
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

    class HintedEnumConstantElementDescriptor(
        private val enum: Enum<*>,
        private val directive: RenderingDirective
    ) : ElementDescriptor {
        override val name: String = enum.name
        override val isRenderedValue: Boolean = true
        override val isHighlight: Boolean = false
        override val highlight: Highlight? = null

        override fun resolveValue(target: Any, path: String?): HintedValue =
            HintedValue(
                value = computeValue(enum),
                hint = computeHint(enum)
            )

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