package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.StateCollector
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class KotlinWithPrimitiveParametersTest : KotlinExampleTest(), WithHamkrest {

    @ParameterizedTest(name = "[{index}] {0}, {1}")
    @MethodSource("primitiveParameters")
    fun echoesTheNumber(count: Int, enabled: Boolean, expected: String) {
        then(theCount(count), equalTo(expected))
    }

    private fun theCount(count: Int) = StateCollector { count.toString() }

    companion object {
        @JvmStatic
        fun primitiveParameters(): Set<Arguments> = setOf(
            Arguments.of(42, true, "42"),
            Arguments.of(7, false, "7")
        )
    }
}
