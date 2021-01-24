package dev.kensa.acceptance.example

import dev.kensa.Highlight
import dev.kensa.NestedSentence
import dev.kensa.Scenario
import dev.kensa.SentenceValue
import dev.kensa.acceptance.example.TestExtension.Companion.MY_PARAMETER_VALUE
import dev.kensa.kotlin.KotlinKensaTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(TestExtension::class)
class KotlinTestWithVariousParameterCombinations : KotlinKensaTest {
    private val field1: String? = null

    @Scenario
    private val field2: String? = null

    @Highlight
    @SentenceValue
    private val field3: String? = null

    @Test
    fun testWithNoParameters() {
        assertThat("true").isEqualTo("true")
    }

    @Test
    fun testWithExtensionParameter(first: TestExtension.MyArgument) {
        assertThat(first.value).isEqualTo(MY_PARAMETER_VALUE)
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "b"])
    fun parameterizedTest(first: String?) {
        assertThat(first).isIn("a", "b")
    }

    @ParameterizedTest
    @ValueSource(strings = ["a", "b"])
    fun parameterizedTestWithExtensionParameter(first: String?, @SentenceValue second: TestExtension.MyArgument) {
        assertThat(first).isIn("a", "b")
        assertThat(second.value).isEqualTo(MY_PARAMETER_VALUE)
    }

    @SentenceValue
    fun method1() {
    }

    @NestedSentence
    fun nested1() {
    }
}