package dev.kensa.context

import dev.kensa.Issue
import dev.kensa.Notes
import dev.kensa.state.TestMethodInvocation
import dev.kensa.state.TestState.*
import dev.kensa.util.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContainerFactory {
    fun createFor(context: ExtensionContext, testFileWriter: (TestContainer) -> Unit): TestContainer {
        return context.requiredTestClass.let { testClass ->
            TestContainer(
                testClass,
                deriveDisplayNameFor(testClass) { testClass.simpleName.unCamel() },
                invocationDataFor(testClass),
                notesFor(testClass),
                issuesFor(testClass),
                testFileWriter
            )
        }
    }

    private fun invocationDataFor(testClass: Class<*>): Map<Method, TestMethodInvocation> =
        testClass.testMethods()
            .map { method: Method -> createInvocationData(method) }
            .associateByTo(LinkedHashMap()) { invocation: TestMethodInvocation -> invocation.method }

    private fun createInvocationData(method: Method): TestMethodInvocation =
        TestMethodInvocation(
            method,
            deriveDisplayNameFor(method) { method.normalisedName.unCamel() },
            notesFor(method),
            issuesFor(method),
            initialStateFor(method)
        )

    private fun initialStateFor(method: Method) = if (method.hasAnnotation<Disabled>()) Disabled else NotExecuted

    private fun deriveDisplayNameFor(element: AnnotatedElement, lazyDefault: () -> String) =
        element.findAnnotation<DisplayName>()?.value ?: lazyDefault()

    private fun notesFor(element: AnnotatedElement): String? = element.findAnnotation<Notes>()?.value

    private fun issuesFor(element: AnnotatedElement): List<String> = element.findAnnotation<Issue>()?.value?.toList() ?: emptyList()
}