package dev.kensa.kotest

import dev.kensa.context.FrameworkDescriptor
import dev.kensa.context.KensaLifecycleManager
import dev.kensa.context.TestContainer
import dev.kensa.parse.kotlin.KotlinParser
import dev.kensa.parse.kotlin.KotlinParserDelegate.Companion.findAnnotationNames
import dev.kensa.state.TestState.Disabled
import dev.kensa.state.TestState.NotExecuted
import dev.kensa.util.findTestMethods
import dev.kensa.util.hasAnnotation
import dev.kensa.util.normalisedPlatformName
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.AfterSpecListener
import io.kotest.core.listeners.BeforeProjectListener
import io.kotest.core.listeners.BeforeSpecListener
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Kensa global listener for Kotest. Register via [io.kotest.core.config.AbstractProjectConfig]:
 *
 * ```kotlin
 * // io/kotest/provided/ProjectConfig.kt
 * object ProjectConfig : AbstractProjectConfig() {
 *     override val extensions = listOf(KensaKotestListener())
 * }
 * ```
 *
 * Uses [TestCaseExtension.intercept] to bind the Kensa thread-local immediately before
 * test body execution, ensuring it is on the same coroutine/thread as the test.
 */
class KensaKotestListener :
    BeforeProjectListener,
    AfterProjectListener,
    BeforeSpecListener,
    AfterSpecListener,
    TestCaseExtension {

    private val containers = ConcurrentHashMap<String, TestContainer>()

    override suspend fun beforeProject() {
        KensaLifecycleManager.initialise(kotestDescriptor)
    }

    override suspend fun beforeSpec(spec: Spec) {
        val testClass = spec::class.java
        KensaLifecycleManager.current()?.beforeClass(testClass, testClass.simpleName)?.let { container ->
            containers[testClass.name] = container
        }
    }

    override suspend fun intercept(testCase: TestCase, execute: suspend (TestCase) -> TestResult): TestResult {
        val manager = KensaLifecycleManager.current() ?: return execute(testCase)
        val testClass = testCase.spec::class.java
        val method = testClass.findMethodForTestCase(testCase) ?: return execute(testCase)

        manager.beforeTest(testClass, method)
        val invocationId = manager.startInvocation(
            testCase.spec,
            testClass,
            method,
            emptyList(),
            testCase.name.name,
        )
        val result = execute(testCase)
        manager.endInvocation(testClass, method, invocationId, result.errorOrNull)
        return result
    }

    override suspend fun afterSpec(spec: Spec) {
        containers.remove(spec::class.java.name)?.let {
            KensaLifecycleManager.current()?.writeTestResult(it)
        }
    }

    override suspend fun afterProject() {
        KensaLifecycleManager.current()?.writeAllResults()
    }

    private fun Class<*>.findMethodForTestCase(testCase: TestCase): Method? =
        findTestMethods { true }
            .firstOrNull { it.normalisedPlatformName == testCase.name.name }
}

private val kotestTestAnnotationNames = listOf("Test", "io.kotest.core.spec.style.AnnotationSpec.Test")

val kotestDescriptor = FrameworkDescriptor(
    findTestMethods = { cs ->
        cs.findTestMethods {
            it.hasAnnotation<io.kotest.core.spec.style.AnnotationSpec.Test>()
        }
    },
    initialStateFor = { md ->
        if (md.hasAnnotation<io.kotest.core.spec.style.AnnotationSpec.Ignore>()) Disabled else NotExecuted
    },
    displayNameFor = { _ -> null },
    isJavaClassTest = { _ -> false },
    isJavaInterfaceTest = { _ -> false },
    isKotlinTest = { context: KotlinParser.FunctionDeclarationContext ->
        context.findAnnotationNames().any { it in kotestTestAnnotationNames }
    },
)
