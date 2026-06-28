package dev.kensa.context

import dev.kensa.Configuration
import dev.kensa.KensaConfigurationProvider
import dev.kensa.example.KotlinWithParameters
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry
import dev.kensa.fixture.parameterFixture
import dev.kensa.state.TestState
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

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
    fun `writeAllResults writes nothing and leaves output dir untouched when no tests ran`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir) // output enabled, but no beforeClass / skipClass called

        manager.writeAllResults()

        tempDir.resolve("kensa-output").shouldNotExist()
    }

    @Test
    fun `writeAllResults preserves a prior report when no tests ran`(@TempDir tempDir: Path) {
        val outputDir = tempDir.resolve("kensa-output").also { it.createDirectories() }
        val sentinel = outputDir.resolve("index.html")
        sentinel.writeText("previous report")

        val manager = createManager(tempDir) // output enabled, but no tests participate

        manager.writeAllResults()

        sentinel.shouldExist()
        sentinel.toFile().readText() shouldBe "previous report"
    }

    @Test
    fun `writeAllResults produces report when a test class participated`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir)
        manager.beforeClass(DummyTest::class.java, "Dummy Test")

        manager.writeAllResults()

        val outputDir = tempDir.resolve("kensa-output")
        outputDir.resolve("index.html").shouldExist()
        outputDir.resolve("indices.json").shouldExist()
    }

    @Test
    fun `writeTestResult is no-op when output disabled`(@TempDir tempDir: Path) {
        val manager = createManager(tempDir, outputEnabled = false)
        val container = TestContainer(String::class.java, "Test", emptyMap(), null, emptyList(), emptyList())

        manager.writeTestResult(container)
    }

    @Test
    fun `startInvocation seeds parameter fixtures from arguments before the body runs`(@TempDir tempDir: Path) {
        FixtureRegistry.clearFixtures()
        FixtureRegistry.registerFixtures(LifecycleSeederFixtures)
        try {
            val method = KotlinWithParameters::class.java.declaredMethods.first { it.name == "parameterizedTest" }
            val descriptor = testDescriptor.copy(
                findTestMethods = { setOf(method) },
                isKotlinTest = { it.simpleIdentifier().text == "parameterizedTest" },
            )
            val manager = createManager(tempDir, descriptor = descriptor) {
                sourceLocations = listOf(java.nio.file.Path.of("src/example/kotlin"))
            }
            manager.beforeClass(KotlinWithParameters::class.java, "Kotlin With Parameters")
            manager.beforeTest(KotlinWithParameters::class.java, method)

            manager.startInvocation(KotlinWithParameters(), KotlinWithParameters::class.java, method, listOf("alice", 1), "test")

            TestContextHolder.testContext().fixtures[LifecycleSeederFixtures.greeting] shouldBe "Hello, alice"

            manager.endInvocation(KotlinWithParameters::class.java, method, null, null)
        } finally {
            FixtureRegistry.clearFixtures()
        }
    }

    private fun createManager(tempDir: Path, outputEnabled: Boolean = true, descriptor: FrameworkDescriptor = testDescriptor, configure: Configuration.() -> Unit = {}): KensaLifecycleManager {
        TestConfigProvider.configuration = Configuration().apply {
            outputDir = tempDir.resolve("kensa-output")
            isOutputEnabled = outputEnabled
            configure()
        }
        System.setProperty(KensaLifecycleManager.CONFIGURATION_PROVIDER_PROPERTY, TestConfigProvider::class.java.name)
        try {
            return KensaLifecycleManager.initialise(descriptor)
        } finally {
            System.clearProperty(KensaLifecycleManager.CONFIGURATION_PROVIDER_PROPERTY)
        }
    }

    @Suppress("unused")
    object LifecycleSeederFixtures : FixtureContainer {
        val greeting = parameterFixture("greeting", from = "first") { name: String -> "Hello, $name" }
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
