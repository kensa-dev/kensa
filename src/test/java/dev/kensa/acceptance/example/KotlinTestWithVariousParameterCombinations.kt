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
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

@ExtendWith(TestExtension::class)
class KotlinTestWithVariousParameterCombinations : KotlinKensaTest {
    private val field1: String? = null

    @Scenario
    private val field2: String? = null

    @Highlight
    @SentenceValue
    private val field3: String? = null

    @Test
    fun similarNameTest() {
        assertThat("true").isEqualTo("true")
    }

    @Test
    fun similarNameTest1() {
        assertThat("string").isNotBlank
    }

    @Test
    fun testWithNoParameters() {
        assertThat("true").isEqualTo("true")
    }

    @Test
    fun testWithExtensionParameter(first: TestExtension.MyArgument) {
        assertThat(first.value).isEqualTo(MY_PARAMETER_VALUE)
    }

    @Test
    internal fun internalTest() {
        assertThat("true").isEqualTo("true")
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

    @ParameterizedTest
    @MethodSource("genericParameters")
    fun testWithGenericExtensionParameter(param: List<Map<String, String>>) {
        assertThat(param.first()).containsKey("a")
    }

    @ParameterizedTest
    @ValueSource(ints = [1])
    fun testWithIntParameter(param: Int) {
        assertThat(param).isEqualTo(1)
    }

    @ParameterizedTest
    @ValueSource(bytes = [1])
    fun testWithByteParameter(param: Byte) {
        assertThat(param).isEqualTo(1)
    }

    @ParameterizedTest
    @ValueSource(longs = [1])
    fun testWithLongParameter(param: Long) {
        assertThat(param).isEqualTo(1)
    }

    @ParameterizedTest
    @ValueSource(shorts = [1])
    fun testWithShortParameter(param: Short) {
        assertThat(param).isEqualTo(1)
    }

    @ParameterizedTest
    @MethodSource("intListParameters")
    fun testWithIntListParameter(param: List<Int>) {
        assertThat(param.first()).isEqualTo(1)
    }

    @ParameterizedTest
    @MethodSource("byteListParameters")
    fun testWithByteListParameter(param: List<Byte>) {
        assertThat(param.first()).isEqualTo(1)
    }

    @SentenceValue
    fun method1() {
    }

    @NestedSentence
    internal fun nested1() {
    }

    @NestedSentence
    internal fun nested123() {
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun genericParameters(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf(mapOf("a" to "b")))
        )

        @Suppress("unused")
        @JvmStatic
        fun intListParameters(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf(1))
        )

        @Suppress("unused")
        @JvmStatic
        fun byteListParameters(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf<Byte>(1))
        )
    }
}