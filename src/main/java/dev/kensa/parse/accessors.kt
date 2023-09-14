package dev.kensa.parse

import dev.kensa.Highlight
import dev.kensa.Scenario
import dev.kensa.SentenceValue
import dev.kensa.ScenarioHolder
import dev.kensa.util.*
import java.lang.System.err
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

private fun throwAnnotationNotFound(annotation: KClass<out Annotation>): Nothing = throw IllegalStateException("Did not find ${annotation.simpleName}")

private fun accessAndLogError(accessor: () -> Any?) : Any? =
    try { accessor() } catch ( e: Exception ) { err.println("Accessor threw an exception: "); e.printStackTrace(err); null }

sealed interface Accessor {
    val name: String
    val isSentenceValue: Boolean
    val isHighlight: Boolean
    val isScenario: Boolean
    val isScenarioHolder: Boolean

    val highlight: Highlight

    sealed interface ValueAccessor : Accessor {
        fun valueOfIn(target: Any): Any?

        open class PropertyAccessor(val property: KProperty<*>) : ValueAccessor {
            override val name: String = property.name
            override val isSentenceValue: Boolean by lazy { property.hasKotlinOrJavaAnnotation<SentenceValue>() }
            override val isHighlight: Boolean by lazy { property.hasKotlinOrJavaAnnotation<Highlight>() }
            override val isScenario: Boolean by lazy { property.hasKotlinOrJavaAnnotation<Scenario>() }
            override val isScenarioHolder: Boolean by lazy { property.hasKotlinOrJavaAnnotation<ScenarioHolder>() }
            override fun valueOfIn(target: Any): Any? = accessAndLogError { property.valueOfKotlinPropertyIn(target) }
            override val highlight by lazy { property.findKotlinOrJavaAnnotation<Highlight>() ?: throwAnnotationNotFound(Highlight::class) }
        }

        class ScenarioHolderAccessor(private val scenarioHolderProperty: KProperty<*>, targetProperty: KProperty<*>) : PropertyAccessor(targetProperty) {
            override fun valueOfIn(target: Any): Any? = accessAndLogError {
                    scenarioHolderProperty.valueOfKotlinPropertyIn(target)?.let {
                        super.valueOfIn(it)
                    }
                }
        }

        class MethodAccessor(val method: Method) : ValueAccessor {
            override val name: String = method.name
            override val isSentenceValue: Boolean by lazy { method.hasAnnotation<SentenceValue>() }
            override val isHighlight: Boolean by lazy { method.hasAnnotation<Highlight>() }
            override val isScenario: Boolean by lazy { method.hasAnnotation<Scenario>() }
            override val isScenarioHolder: Boolean = false
            override fun valueOfIn(target: Any): Any? = accessAndLogError { target.invokeMethod(name) }
            override val highlight by lazy { method.findAnnotation<Highlight>() ?: throwAnnotationNotFound(Highlight::class) }
        }
    }

    data class ParameterAccessor(val parameter: Parameter, override val name: String, val index: Int, val isCaptured: Boolean) : Accessor {
        override val isSentenceValue: Boolean by lazy { parameter.hasAnnotation<SentenceValue>() }
        override val isHighlight: Boolean by lazy { parameter.hasAnnotation<Highlight>() }
        override val isScenario: Boolean = false
        override val isScenarioHolder: Boolean = false
        override val highlight by lazy { parameter.findAnnotation<Highlight>() ?: throwAnnotationNotFound(Highlight::class) }
    }
}
