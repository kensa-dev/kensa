package dev.kensa.context

import dev.kensa.*
import dev.kensa.state.TestInvocationFactory
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import dev.kensa.util.*
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.LinkedHashMap

class TestContainerFactory(
    private val initialStateFor: (AnnotatedElement) -> TestState,
    private val displayNameFor: (AnnotatedElement) -> String?,
    private val findTestMethods: (Class<*>) -> Set<Method>,
    private val testInvocationFactory: TestInvocationFactory,
    private val configuration: Configuration
) {

    fun createFor(testClass: Class<*>, displayName: String, commonBasePackage: String = ""): TestContainer =
        testClass.run {
            TestContainer(
                this,
                displayName.unCamel(),
                createMethodContainers(),
                notes(),
                issues(),
                deriveMinimumUniquePackageName(commonBasePackage),
            )
        }

    private fun Class<*>.createMethodContainers(): Map<Method, TestMethodContainer> =
        findTestMethods(this)
            .map { method: Method -> method.createMethodContainer(autoOpenTab()) }
            .associateByTo(HashMap()) { invocation: TestMethodContainer -> invocation.method }

    private fun Method.createMethodContainer(autoOpenTab: Tab): TestMethodContainer =
        TestMethodContainer(
            testInvocationFactory,
            this,
            deriveDisplayName { normalisedPlatformName.unCamel() },
            notes(),
            issues(),
            initialState(),
            autoOpenTab(autoOpenTab)
        )

    private fun Method.initialState() = initialStateFor(this)

    private fun AnnotatedElement.autoOpenTab(default: Tab? = null): Tab = findAnnotation<AutoOpenTab>()?.value ?: default ?: configuration.autoOpenTab

    private fun AnnotatedElement.deriveDisplayName(lazyDefault: () -> String) = displayNameFor(this) ?: lazyDefault()

    private fun AnnotatedElement.notes(): String? = findAnnotation<Notes>()?.value

    private fun AnnotatedElement.issues(): List<String> = findAnnotation<Issue>()?.value?.toList() ?: emptyList()

    private fun <T> Class<T>.deriveMinimumUniquePackageName(commonBasePackage: String): String =
        commonBasePackage
            .takeIf { it.isNotBlank() and packageName.startsWith(it) }
            ?.let { packageName.replaceFirst(it, "") }
            ?.removeLeadingDots()
            ?: packageName

    private fun String.removeLeadingDots(): String = replace(greedyDotRegex, "")

    companion object {
        private val greedyDotRegex = Regex("^\\.+")
    }
}