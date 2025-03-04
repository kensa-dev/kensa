package dev.kensa.context

import dev.kensa.*
import dev.kensa.output.TestWriter
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState.*
import dev.kensa.util.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContainerFactory {
    fun createFor(context: ExtensionContext, testWriter: TestWriter, commonBasePackage: String = ""): TestContainer =
        context.requiredTestClass.run {
            TestContainer(
                this,
                deriveDisplayName { simpleName.unCamel() },
                createMethodContainers(),
                notes(),
                issues(),
                deriveMinimumUniquePackageName(commonBasePackage),
                testWriter
            )
        }

    private fun Class<*>.createMethodContainers(): Map<Method, TestMethodContainer> =
        testMethods()
            .map { method: Method -> method.createMethodContainer(autoOpenTab()) }
            .associateByTo(LinkedHashMap()) { invocation: TestMethodContainer -> invocation.method }

    private fun Method.createMethodContainer(autoOpenTab: Tab): TestMethodContainer =
        TestMethodContainer(
            this,
            deriveDisplayName { normalisedPlatformName.unCamel() },
            notes(),
            issues(),
            initialState(),
            autoOpenTab(autoOpenTab)
        )

    private fun Method.initialState() = if (hasAnnotation<Disabled>()) Disabled else NotExecuted

    private fun AnnotatedElement.autoOpenTab(default: Tab? = null) : Tab = findAnnotation<AutoOpenTab>()?.value ?: default ?: Kensa.configuration.autoOpenTab

    private fun AnnotatedElement.deriveDisplayName(lazyDefault: () -> String) = findAnnotation<DisplayName>()?.value ?: lazyDefault()

    private fun AnnotatedElement.notes(): String? = findAnnotation<Notes>()?.value

    private fun AnnotatedElement.issues(): List<String> = findAnnotation<Issue>()?.value?.toList() ?: emptyList()

    private fun <T> Class<T>.deriveMinimumUniquePackageName(commonBasePackage: String): String =
        commonBasePackage
            .takeIf { it.isNotBlank() and packageName.startsWith(it) }
            ?.let { packageName.replaceFirst(it, "") }
            ?.removeLeadingDots()
            ?: packageName

    private fun String.removeLeadingDots() =
        replace(Regex("^\\.+"), "")
}