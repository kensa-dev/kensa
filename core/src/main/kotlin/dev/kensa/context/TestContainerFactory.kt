package dev.kensa.context

import dev.kensa.*
import dev.kensa.state.TestInvocationFactory
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import dev.kensa.util.derivedTestName
import dev.kensa.util.findAnnotation
import dev.kensa.util.unCamel
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContainerFactory(
    private val initialStateFor: (AnnotatedElement) -> TestState,
    private val displayNameFor: (AnnotatedElement) -> String?,
    private val findTestMethods: (Class<*>) -> Set<Method>,
    private val testInvocationFactory: TestInvocationFactory,
    private val configuration: Configuration
) {

    fun createFor(testClass: Class<*>, displayName: String): TestContainer =
        testClass.run {
            TestContainer(
                this,
                displayName.unCamel(configuration.dictionary.protectedPhraseValues),
                createMethodContainers(),
                notes(),
                issues(),
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
            deriveDisplayName { derivedTestName(configuration.dictionary.protectedPhraseValues) },
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
}