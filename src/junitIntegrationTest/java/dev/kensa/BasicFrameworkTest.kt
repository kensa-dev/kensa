package dev.kensa

import dev.kensa.KensaTestExecutor.executeTests
import dev.kensa.example.KotlinTestWithVariousParameterCombinations
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.FAILED
import java.nio.file.Files.exists

internal class BasicFrameworkTest : KensaAcceptanceTest() {

    @Test
    internal fun createsOutputFilesWhenMultipleTestClassesExecuted() {
        val testClasses =
            arrayOf<Class<*>>(dev.kensa.example.TestWithSinglePassingTest::class.java, dev.kensa.example.TestWithMultiplePassingTests::class.java)

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
        val results = executeTests(dev.kensa.example.JavaTestWithVariousParameterCombinations::class.java)

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
}