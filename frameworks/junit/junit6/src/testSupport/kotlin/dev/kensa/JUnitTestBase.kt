package dev.kensa

import dev.kensa.junit.KensaExtension.Companion.CONFIGURATION_PROVIDER_PROPERTY
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

@ExtendWith(ExtensionContextParameterResolver::class)
abstract class JUnitTestBase(private val outputPrefix: String) {

    protected lateinit var kensaOutputDir: Path

    @BeforeEach
    fun setUp(context: ExtensionContext) {
        val configuration = Configuration().apply {
            ThreadLocalKensaConfigurationProvider.set(this)
        }

        kensaOutputDir = createAndSetOutputDir(configuration, outputPrefix, context.requiredTestClass, context.requiredTestMethod.name.substringBefore("$"))
    }

    companion object {

        init {
            System.setProperty(CONFIGURATION_PROVIDER_PROPERTY, "dev.kensa.ThreadLocalKensaConfigurationProvider")
        }

        private lateinit var parentDir: Path

        @OptIn(ExperimentalPathApi::class)
        @BeforeAll
        @JvmStatic
        fun createOutputParentDir() {
            synchronized(this) {
                if (!::parentDir.isInitialized) {
                    parentDir = Paths.get("build", "example-output")
                        .also { it.deleteRecursively() }
                }
            }
        }

        fun createAndSetOutputDir(configuration: Configuration, outputPrefix: String, testClass: Class<*>, testMethod: String): Path =
            parentDir.resolve(Paths.get(outputPrefix, testClass.simpleName, testMethod, "kensa-output"))
                .also {
                    configuration.outputDir = it
                }
    }
}