package dev.kensa.acceptance

import dev.kensa.Kensa
import dev.kensa.acceptance.KensaTestExecutor.executeTests
import dev.kensa.acceptance.example.JavaTestWithVariousParameterCombinations
import dev.kensa.acceptance.example.KotlinTestWithVariousParameterCombinations
import dev.kensa.acceptance.example.TestWithMultiplePassingTests
import dev.kensa.acceptance.example.TestWithSinglePassingTest
import dev.kensa.context.TestContainer
import dev.kensa.output.DefaultResultWriter
import dev.kensa.output.ResultWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.FAILED
import java.nio.file.Files.exists

internal class BasicFrameworkTest : KensaAcceptanceTest() {

    @BeforeEach
    @AfterEach
    internal fun resetKensaResultWriter() {
        Kensa.configure().withResultWriter(DefaultResultWriter())
    }

    @Test
    internal fun createsOutputFilesWhenMultipleTestClassesExecuted() {
        val testClasses =
            arrayOf<Class<*>>(TestWithSinglePassingTest::class.java, TestWithMultiplePassingTests::class.java)

        executeTests(*testClasses)

        testClasses.forEach {
            val outputFilePath = kensaOutputDir.resolve(it.name + ".html")

            assertThat(exists(outputFilePath))
                .withFailMessage("Expected file [%s] to exist.", outputFilePath)
                .isTrue
        }
        assertThat(exists(kensaOutputDir.resolve("index.html"))).isTrue
        assertThat(exists(kensaOutputDir.resolve("kensa.js"))).isTrue
    }

    @Test
    internal fun handlesJavaTestsWithVariousParameterCombinations() {
        val results = executeTests(JavaTestWithVariousParameterCombinations::class.java)

        val failedTests = results.testEvents().failed()
            .map { it.getPayload(TestExecutionResult::class.java).orElseThrow() }
            .filter { it.status == FAILED }

        assertThat(failedTests).isEmpty()
    }

    @Test
    internal fun handlesKotlinTestsWithVariousParameterCombinations() {
        val results = executeTests(KotlinTestWithVariousParameterCombinations::class.java)

        val failedTests = results.testEvents().failed()
            .map { it.getPayload(TestExecutionResult::class.java).orElseThrow() }
            .filter { it.status == FAILED }

        assertThat(failedTests).isEmpty()
    }

    @Test
    internal fun usesConfiguredResultWriter() {
        val resultWriter = OverriddenResultWriter()
        Kensa.configure().withResultWriter(resultWriter)

        executeTests(TestWithSinglePassingTest::class.java)

        assertThat(resultWriter.getContainers()).isNotEmpty
    }

    private class OverriddenResultWriter : ResultWriter {
        private var containers: Set<TestContainer>? = null

        override fun write(containers: Set<TestContainer>) {
            this.containers = containers
        }

        fun getContainers(): Set<TestContainer>? {
            return containers
        }
    }

}