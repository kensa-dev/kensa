package dev.kensa

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import dev.kensa.KensaTestExecutor.executeAllTestsIn
import dev.kensa.KensaTestExecutor.executeTests
import dev.kensa.example.*
import dev.kensa.example.hints.*
import dev.kensa.extension.TestParameterResolver.MyArgument
import dev.kensa.junit.KensaTest
import dev.kensa.sentence.Acronym
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.platform.launcher.listeners.TestExecutionSummary
import kotlin.io.path.Path
import kotlin.io.path.readText

internal class JUnitWithKotlinFrameworkTest : JUnitTestBase("Kotlin") {

    @AfterEach
    fun afterEach() {
        ThreadLocalKensaConfigurationProvider.remove()
    }

    @Nested
    inner class FileCreation {

        @Test
        fun createsOutputFilesCorrectly() {
            testConfiguration {
                sourceLocations = listOf(Path("src/kotlinExample/kotlin"))
            }

            val testClasses: Array<Class<out KensaTest>> = arrayOf(
                KotlinWithSinglePassingTest::class.java,
                KotlinWithMultiplePassingTests::class.java
            )

            val result = executeTests(*testClasses)
            result.verifyZeroFailures()

            verifyFilesExist(testClasses)
        }

        @Test
        fun doesNotCreateOutputFilesWhenOutputIsDisabled() {
            testConfiguration {
                sourceLocations = listOf(Path("src/kotlinExample/kotlin"))
                isOutputEnabled = false
            }

            val result = executeAllTestsIn(KotlinWithOutputDisabledTest::class.java)
            result.verifyZeroFailures()

            with(kensaOutputDir) {
                resolve("configuration.json").shouldNotExist()
                resolve("index.html").shouldNotExist()
                resolve("indices.json").shouldNotExist()
                resolve("kensa2.js").shouldNotExist()
                resolve("logo.svg").shouldNotExist()
                resolve(Path("results", "dev.kensa.${KotlinWithOutputDisabledTest::class.simpleName}.json")).shouldNotExist()
            }
        }

        @Test
        fun createsOutputFileForDisabledClass() {
            testConfiguration {
                sourceLocations = listOf(Path("src/kotlinExample/kotlin"))
            }

            executeAllTestsIn(KotlinWithDisabledClassTest::class.java)

            with(kensaOutputDir) {
                resolve(Path("results", "${KotlinWithDisabledClassTest::class.java.name}.json")).shouldExist()
            }
        }

        private fun verifyFilesExist(testClasses: Array<Class<out KensaTest>>) {
            with(kensaOutputDir) {
                testClasses.forEach {
                    resolve(Path("results", "${it.kotlin.qualifiedName}.json")).shouldExist()
                }
                resolve("configuration.json").shouldExist()
                resolve("index.html").shouldExist()
                resolve("indices.json").shouldExist()
                resolve("kensa2.js").shouldExist()
                resolve("logo.svg").shouldExist()
            }
        }
    }

    @Nested
    inner class JsonOutput {

        @ParameterizedTest(name = "Test class name: {0}")
        @ValueSource(
            classes = [
                KotlinWithSinglePassingTest::class,
                KotlinWithGenericParameterizedTest::class,
                KotlinWithExpandableSentenceTest::class,
                KotlinWithLiteralsTest::class,
                KotlinWithTypeArgumentsTest::class,
                KotlinWithCapturedOutputsTest::class,
                KotlinWithSetupStepsTest::class,
                KotlinWithVariousNamingTest::class,
                KotlinWithJavaRecordTest::class,
                KotlinWithHintedFieldsInsideTest::class,
                KotlinWithHintedFieldsInsideObjectTest::class,
                KotlinWithMethodHintStrategyTest::class,
                KotlinWithMixedStrategyTest::class,
                KotlinWithPropertyStrategyTest::class,
                KotlinWithNotesTest::class
            ]
        )
        fun embeddedJsonIsCorrectFor(theTestClass: Class<*>) {
            testConfiguration {
                sourceLocations = listOf(Path("src/kotlinExample/kotlin"))
            }
            executeTestAndVerifyJson(theTestClass)
        }

        @Test
        fun embeddedJsonIsCorrectForTestWithExtensionParameter() {
            testConfiguration {
                sourceLocations = listOf(Path("src/kotlinExample/kotlin"))
                renderers.addValueRenderer(MyArgument::class.java, MyArgumentRenderer)
            }
            executeTestAndVerifyJson(KotlinWithParameterResolverExtensionTest::class.java)
        }

        @Test
        fun embeddedJsonIsCorrectForTestWithVariousAnnotationsParameter() {
            testConfiguration {
                sourceLocations = listOf(Path("src/kotlinExample/kotlin"))
                renderers.addValueRenderer(MyArgument::class.java, MyArgumentRenderer)
                renderers.addValueRenderer(MyThing::class.java) {
                    """MyThing"""
                }
            }
            executeTestAndVerifyJson(KotlinWithAnnotationFeatureTest::class.java)
        }

        @Test
        fun embeddedJsonIsCorrectForTestWithAcronyms() {
            testConfiguration {
                sourceLocations = listOf(Path("src/kotlinExample/kotlin"))
                acronyms(Acronym.of("FTTP", "Fibre To The Premises"))
                acronyms(Acronym.of("FUBAR", "F***** up beyond all recognition"))
            }
            executeTestAndVerifyJson(KotlinWithAcronymsTest::class.java)
        }

        private fun executeTestAndVerifyJson(testClass: Class<*>) {
            executeAllTestsIn(testClass)
                .verifyZeroFailures()

            val expectedResultJson = testClass.json("result").cleanseForComparison("elapsedTime")
            val path = kensaOutputDir.resolve("results").resolve("${testClass.name}.json")
            val resultJson = path.readText().cleanseForComparison("elapsedTime")
            resultJson.shouldEqualJson(expectedResultJson)
        }

        private fun String.cleanseForComparison(vararg removeFields: String): String =
            Json.parse(this)
                .cleanseForComparison(*removeFields)
                .toString()

        private fun JsonValue.cleanseForComparison(vararg removeFields: String) = apply {
            cleanse(removeFields.toSet())
        }

        private fun JsonValue.cleanse(fieldsToRemove: Set<String>) {
            when {
                isObject -> {
                    val jsonObject = asObject()

                    fieldsToRemove.forEach { field -> jsonObject.remove(field) }

                    jsonObject.forEach { child: JsonObject.Member ->
                        child.value.cleanse(fieldsToRemove)
                    }
                }

                isArray -> {
                    asArray().forEach { element ->
                        element.cleanse(fieldsToRemove)
                    }
                }
            }
        }

        private fun Class<*>.json(id: String) = getResource("/kotlin/${simpleName}_$id.json")?.readText() ?: throw IllegalStateException("Unable to find resource ${simpleName}_$id.json")
    }

    private fun TestExecutionSummary.verifyZeroFailures() {
        val failCount = totalFailureCount
        if (failCount > 0) {
            val firstThrowable = failures.firstOrNull()?.exception
            throw AssertionError(
                if (failCount == 1L) "There was 1 unexpected failure: ${firstThrowable?.message ?: "Unknown error"}"
                else "There were $failCount unexpected failures, the first of which was: ${firstThrowable?.message ?: "Unknown error"}"
            )
        }
    }
}