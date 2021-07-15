package dev.kensa.context

import dev.kensa.Issue
import dev.kensa.Notes
import dev.kensa.parse.normaliseName
import dev.kensa.state.TestMethodInvocation
import dev.kensa.state.TestState.*
import dev.kensa.util.Reflect
import dev.kensa.util.Strings
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContainerFactory {
    fun createFor(context: ExtensionContext): TestContainer {
        return context.requiredTestClass.let { testClass ->
            TestContainer(
                testClass,
                deriveDisplayNameFor(testClass) { Strings.unCamel(testClass.simpleName) },
                invocationDataFor(testClass),
                notesFor(testClass),
                issuesFor(testClass)
            )
        }
    }

    private fun invocationDataFor(testClass: Class<*>): Map<Method, TestMethodInvocation> {
        return Reflect.testFunctionsOf(testClass)
            .map { method: Method -> createInvocationData(method) }
            .associateByTo(LinkedHashMap()) { invocation: TestMethodInvocation -> invocation.method }
    }

    private fun createInvocationData(method: Method): TestMethodInvocation {
        return TestMethodInvocation(
            method,
            deriveDisplayNameFor(method) { Strings.unCamel(method.normaliseName()) },
            notesFor(method),
            issuesFor(method),
            initialStateFor(method)
        )
    }

    private fun initialStateFor(method: Method) = if (Reflect.hasAnnotation<Disabled>(method)) Disabled else NotExecuted

    private fun deriveDisplayNameFor(element: AnnotatedElement, lazyDefault: () -> String) =
        Reflect.findAnnotation<DisplayName>(element)?.value ?: lazyDefault()

    private fun notesFor(element: AnnotatedElement): String? = Reflect.findAnnotation<Notes>(element)?.value

    private fun issuesFor(element: AnnotatedElement): List<String> =
        Reflect.findAnnotation<Issue>(element)?.value?.toList() ?: emptyList()
}