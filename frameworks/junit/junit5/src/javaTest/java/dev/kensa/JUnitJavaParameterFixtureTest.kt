package dev.kensa

import dev.kensa.KensaTestExecutor.executeAllTestsIn
import dev.kensa.example.JavaWithParameterFixtureTest
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.readText

internal class JUnitJavaParameterFixtureTest : JUnitTestBase("Java") {

    @Test
    fun `java parameter fixture renders the per-invocation derived value in the sentence`() {
        testConfiguration {
            sourceLocations = listOf(Path("src/javaExample/java"))
        }

        executeAllTestsIn(JavaWithParameterFixtureTest::class.java).totalFailureCount shouldBeExactly 0

        val resultJson = kensaOutputDir.resolve("results").resolve("${JavaWithParameterFixtureTest::class.java.name}.json").readText()

        resultJson shouldContain "Hello, alice"
        resultJson shouldContain "Hello, bob"
    }
}
