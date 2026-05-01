package dev.kensa.context

import dev.kensa.Configuration
import dev.kensa.KensaConfigurationProvider
import dev.kensa.state.TestState
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal class KensaLifecycleManagerTest {

    private val testDescriptor = FrameworkDescriptor(
        initialStateFor = { TestState.NotExecuted },
        displayNameFor = { null },
        findTestMethods = { emptySet() },
        isJavaClassTest = { false },
        isJavaInterfaceTest = { false },
        isKotlinTest = { false },
    )

    @Test
    fun `beforeClass creates TestContainer when output enabled`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir)

        val container = manager.beforeClass(String::class.java, "String Test")

        container.shouldNotBeNull()
        container.testClass shouldBe String::class.java
    }

    @Test
    fun `beforeClass returns null when output disabled`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir, outputEnabled = false)

        val container = manager.beforeClass(String::class.java, "String Test")

        container.shouldBeNull()
    }

    @Test
    fun `beforeTest binds thread locals and endInvocation clears them`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir, outputEnabled = false)
        val method = DummyTest::class.java.getMethod("dummyMethod")

        manager.beforeTest(DummyTest::class.java, method)

        TestContextHolder.testContext().shouldNotBeNull()

        manager.endInvocation(DummyTest::class.java, method, null, null)

        runCatching { TestContextHolder.testContext() }.isFailure shouldBe true
    }

    @Test
    fun `endInvocation clears thread locals even when exception occurs`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir, outputEnabled = false)
        val method = DummyTest::class.java.getMethod("dummyMethod")

        manager.beforeTest(DummyTest::class.java, method)

        manager.endInvocation(DummyTest::class.java, method, null, RuntimeException("test"))

        runCatching { TestContextHolder.testContext() }.isFailure shouldBe true
    }

    @Test
    fun `startInvocation returns null when output disabled`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir, outputEnabled = false)

        val id = manager.startInvocation(Any(), String::class.java, DummyTest::class.java.getMethod("dummyMethod"), emptyList(), "test")

        id.shouldBeNull()
    }

    @Test
    fun `writeAllResults is no-op when output disabled`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir, outputEnabled = false)

        manager.writeAllResults()
    }

    @Test
    fun `writeTestResult is no-op when output disabled`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir, outputEnabled = false)
        val container = TestContainer(String::class.java, "Test", emptyMap(), null, emptyList())

        manager.writeTestResult(container)
    }

private fun createManager(tempDir: Path, outputEnabled: Boolean = true): KensaLifecycleManager {
        TestConfigProvider.configuration = Configuration().apply {
            outputDir = tempDir.resolve("kensa-output")
            isOutputEnabled = outputEnabled
        }
        System.setProperty(KensaLifecycleManager.CONFIGURATION_PROVIDER_PROPERTY, TestConfigProvider::class.java.name)
        try {
            return KensaLifecycleManager.initialise(testDescriptor)
        } finally {
            System.clearProperty(KensaLifecycleManager.CONFIGURATION_PROVIDER_PROPERTY)
        }
    }

    @Suppress("unused")
    class DummyTest {
        fun dummyMethod() {}
    }
}

class TestConfigProvider : KensaConfigurationProvider {
    override fun invoke(): Configuration = configuration

    companion object {
        lateinit var configuration: Configuration
    }
}
