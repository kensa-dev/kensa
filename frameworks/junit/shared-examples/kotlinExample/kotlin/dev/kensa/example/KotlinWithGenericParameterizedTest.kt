package dev.kensa.example

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import dev.kensa.RenderedValue
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullSource

class KotlinWithGenericParameterizedTest : KotlinExampleTest(), WithHamkrest {
    @RenderedValue
    private val aValue = "aStringValue"

    @ParameterizedTest
    @MethodSource("genericParameters")
    fun theTest(param: List<Map<String, String>>) {
        assertThat(param.get(0), hasKey("a"))
    }

    @ParameterizedTest
    @NullSource
    fun theTestWithOptionalParameterAndClosingFunctionBraceOnSameLineAsParameter(param: String?) {
        assertThat(param, absent())
    }

    @ParameterizedTest
    @NullSource
    fun theTestWithOptionalParameterAndClosingFunctionBraceOnDifferentLineAsParameter(
        param: String?
    ) {
        assertThat(param, absent())
    }

    private fun hasKey(key: String) = Matcher(Map<String, String>::containsKey, key)

    companion object {
        @JvmStatic
        fun genericParameters(): Set<Arguments?> = setOf(Arguments.of(listOf(mapOf("a" to "b"))))
    }
}
