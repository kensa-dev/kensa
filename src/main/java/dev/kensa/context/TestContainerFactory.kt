package dev.kensa.context

import dev.kensa.Issue
import dev.kensa.Notes
import dev.kensa.state.TestMethodInvocation
import dev.kensa.state.TestState.*
import dev.kensa.util.Reflect
import dev.kensa.util.Strings
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

class TestContainerFactory {
    fun createFor(context: ExtensionContext): TestContainer {
        return context.requiredTestClass.let { testClass ->
            val kClass = testClass.kotlin
            TestContainer(
                    kClass,
                    deriveDisplayNameFor(kClass) { Strings.unCamel(testClass.simpleName) },
                    invocationDataFor(testClass),
                    notesFor(kClass),
                    issuesFor(kClass)
            )
        }
    }

    private fun invocationDataFor(testClass: Class<*>): Map<KFunction<*>, TestMethodInvocation> {
        return Reflect.testFunctionsOf(testClass.kotlin)
                .map { kFunction: KFunction<*> -> createInvocationData(kFunction) }
                .associateByTo(LinkedHashMap()) { invocation: TestMethodInvocation -> invocation.testFunction }
    }

    private fun createInvocationData(kFunction: KFunction<*>): TestMethodInvocation {
        return TestMethodInvocation(
                kFunction,
                deriveDisplayNameFor(kFunction) { Strings.unCamel(kFunction.name) },
                notesFor(kFunction),
                issuesFor(kFunction),
                initialStateFor(kFunction)
        )
    }

    private fun initialStateFor(kFunction: KFunction<*>) = kFunction.findAnnotation<Disabled>()?.let { Disabled } ?: NotExecuted

    private fun deriveDisplayNameFor(kClass: KClass<*>, lazyDefault: () -> String) = kClass.findAnnotation<DisplayName>()?.value ?: lazyDefault()

    private fun deriveDisplayNameFor(kFunction: KFunction<*>, lazyDefault: () -> String) = kFunction.findAnnotation<DisplayName>()?.value ?: lazyDefault()

    private fun notesFor(kClass: KClass<*>): String? = kClass.findAnnotation<Notes>()?.value

    private fun notesFor(kFunction: KFunction<*>): String? = kFunction.findAnnotation<Notes>()?.value

    private fun issuesFor(kClass: KClass<*>): List<String> = kClass.findAnnotation<Issue>()?.value?.toList() ?: emptyList()

    private fun issuesFor(kFunction: KFunction<*>): List<String> = kFunction.findAnnotation<Issue>()?.value?.toList() ?: emptyList()
}