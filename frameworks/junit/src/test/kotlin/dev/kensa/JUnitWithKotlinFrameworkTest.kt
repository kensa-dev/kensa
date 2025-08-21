package dev.kensa

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import dev.kensa.KensaTestExecutor.executeAllTestsIn
import dev.kensa.KensaTestExecutor.executeTests
import dev.kensa.extension.TestParameterResolver.MyArgument
import dev.kensa.junit.KensaTest
import dev.kensa.sentence.Acronym
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import org.jsoup.Jsoup
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.testkit.engine.EngineExecutionResults
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.jvm.optionals.getOrNull

internal class JUnitWithKotlinFrameworkTest : JUnitTestBase("Kotlin") {

    @Nested
    inner class FileCreation {

        @Test
        fun createsOutputFilesCorrectlyWhenPackagesNotFlattened() {
            testConfiguration {
                flattenOutputPackages = false
            }

            val testClasses = arrayOf(
                KotlinWithSinglePassingTest::class.java,
                KotlinWithMultiplePassingTests::class.java
            )

            val result = executeTests(*testClasses)

            result.verifyZeroFailures()

            verifyFilesExist(testClasses, "dev/kensa/")
        }

        @Test
        fun createsOutputFilesCorrectlyWhenPackagesAreFlattened() {
            testConfiguration {
                flattenOutputPackages = true
            }

            val testClasses = arrayOf(
                KotlinWithSinglePassingTest::class.java,
                KotlinWithMultiplePassingTests::class.java
            )

            val result = executeTests(*testClasses)
            result.verifyZeroFailures()

            verifyFilesExist(testClasses, "dev.kensa.")
        }

        @Test
        fun doesNotCreateOutputFilesWhenOutputIsDisabled() {
            testConfiguration {
                isOutputEnabled = false
            }

            val result = executeAllTestsIn(KotlinWithOutputDisabledTest::class.java)
            result.verifyZeroFailures()

            with(kensaOutputDir) {
                resolve(Path("dev.kensa." + KotlinWithOutputDisabledTest::class.simpleName + ".html")).shouldNotExist()
                resolve("index.html").shouldNotExist()
                resolve("kensa.js").shouldNotExist()
                resolve("favicon.ico").shouldNotExist()
            }
        }

        private fun verifyFilesExist(testClasses: Array<Class<out KensaTest>>, pathOrFilePrefix: String) {
            with(kensaOutputDir) {
                testClasses.forEach {
                    resolve(Path(pathOrFilePrefix + it.simpleName + ".html")).shouldExist()
                }
                resolve("index.html").shouldExist()
                resolve("kensa.js").shouldExist()
                resolve("favicon.ico").shouldExist()
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
                KotlinWithNestedSentenceTest::class,
                KotlinWithLiteralsTest::class,
                KotlinWithTypeArgumentsTest::class,
                KotlinWithCapturedOutputsTest::class,
                KotlinWithCapturedOutputsTest::class,
                KotlinWithSetupStepsTest::class,
                KotlinWithVariousNamingTest::class,
                KotlinWithJavaRecordTest::class,
            ]
        )
        fun embeddedJsonIsCorrectFor(theTestClass: Class<*>) {
            executeTestAndVerifyJson(theTestClass)
        }

        @Test
        fun embeddedJsonIsCorrectForTestWithExtensionParameter() {
            testConfiguration {
                renderers.addValueRenderer(MyArgument::class.java, MyArgumentRenderer)
            }
            executeTestAndVerifyJson(KotlinWithParameterResolverExtensionTest::class.java)
        }

        @Test
        fun embeddedJsonIsCorrectForTestWihVariousAnnotationsParameter() {
            testConfiguration {
                renderers.addValueRenderer(MyArgument::class.java, MyArgumentRenderer)
            }
            executeTestAndVerifyJson(KotlinWithAnnotationFeatureTest::class.java)
        }

        @Test
        fun embeddedJsonIsCorrectForTestWithAcronyms() {
            testConfiguration {
                acronyms(Acronym.of("FTTP", "Fibre To The Premises"))
                acronyms(Acronym.of("FUBAR", "F***** up beyond all recognition"))
            }
            executeTestAndVerifyJson(KotlinWithAcronymsTest::class.java)
        }

        private fun executeTestAndVerifyJson(testClass: Class<*>) {
            executeAllTestsIn(testClass)
                .verifyZeroFailures()

            val expectedConfigJson = testClass.json("config")
            val expectedResultJson = testClass.json("result").cleanseForComparison("elapsedTime")
            val path = kensaOutputDir.resolve(Path("dev/kensa/" + testClass.simpleName + ".html"))
            val configJson = path.extractJsonFromHtml("config")
            val resultJson = path.extractJsonFromHtml("test-result-1").cleanseForComparison("elapsedTime")

            configJson.shouldEqualJson(expectedConfigJson)
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

        fun Path.extractJsonFromHtml(id: String): String =
            Jsoup.parse(this).select("script#$id").html().trim()
    }

    private fun EngineExecutionResults.verifyZeroFailures() {
        val testEvents = testEvents()
        val failCount = testEvents.failed().count()

        if (failCount > 0) {
            val firstEvent = testEvents.failed().list().first()
            val firstThrowable = firstEvent.getRequiredPayload(TestExecutionResult::class.java).throwable.getOrNull()

            throw AssertionError(failureMessage(failCount, firstThrowable))
        }
    }

    fun failureMessage(failCount: Long, firstThrowable: Throwable?): String =
        if (failCount == 1L) {
            "There was 1 unexpected failure: ${firstThrowable?.message ?: "Unknown error"}"
        } else {
            "There were $failCount unexpected failures, the first of which was: ${firstThrowable?.message ?: "Unknown error"}"
        }
}