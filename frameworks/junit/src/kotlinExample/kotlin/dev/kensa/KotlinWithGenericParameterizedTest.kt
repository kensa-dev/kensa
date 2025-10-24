package dev.kensa

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.List
import java.util.Map

class KotlinWithGenericParameterizedTest : KensaTest, WithHamkrest {
    @RenderedValue
    private val aValue = "aStringValue"

    @ParameterizedTest
    @MethodSource("genericParameters")
    fun theTest(param: List<Map<String, String>>) {
        assertThat(param.get(0), hasKey("a"))
    }

    private fun hasKey(key: String) = Matcher(Map<String, String>::containsKey, key)

    companion object {
        @JvmStatic
        fun genericParameters(): Set<Arguments?> = setOf(Arguments.of(listOf(mapOf("a" to "b"))))
    }
}
