package dev.kensa.context

import dev.kensa.*
import dev.kensa.output.TestWriter
import dev.kensa.state.TestMethodInvocation
import dev.kensa.state.TestState.*
import dev.kensa.util.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContainerFactory {
    fun createFor(context: ExtensionContext, testWriter: TestWriter): TestContainer =
        context.requiredTestClass.let { testClass ->
            TestContainer(
                testClass,
                testClass.deriveDisplayName { testClass.simpleName.unCamel() },
                invocationDataFor(testClass),
                testClass.notes(),
                testClass.issues(),
                testWriter
            )
        }

    private fun invocationDataFor(testClass: Class<*>): Map<Method, TestMethodInvocation> =
        testClass.testMethods()
            .map { method: Method -> method.createInvocationData(testClass.autoOpenTab()) }
            .associateByTo(LinkedHashMap()) { invocation: TestMethodInvocation -> invocation.method }

    private fun Method.createInvocationData(autoOpenTab: Tab): TestMethodInvocation =
        TestMethodInvocation(
            this,
            deriveDisplayName { normalisedName.unCamel() },
            notes(),
            issues(),
            initialState(),
            autoOpenTab(autoOpenTab)
        )

    private fun Method.initialState() = if (hasAnnotation<Disabled>()) Disabled else NotExecuted

    private fun AnnotatedElement.autoOpenTab(default: Tab? = null) : Tab = findAnnotation<AutoOpenTab>()?.value ?: default ?: Kensa.configuration.autoOpenTab

    private fun AnnotatedElement.deriveDisplayName(lazyDefault: () -> String) =
        findAnnotation<DisplayName>()?.value ?: lazyDefault()

    private fun AnnotatedElement.notes(): String? = findAnnotation<Notes>()?.value

    private fun AnnotatedElement.issues(): List<String> = findAnnotation<Issue>()?.value?.toList() ?: emptyList()
}