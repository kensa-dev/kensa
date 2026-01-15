package dev.kensa.example

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.RenderedValue
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinWithSingleFailingInvocationOfParameterizedTest : KotlinExampleTest(), WithHamkrest {
    @RenderedValue
    private val aValue = "correct"

    @ParameterizedTest
    @ValueSource(strings = ["correct", "wrong", "correct"])
    fun theTest(parameter1: String) {
        assertThat(parameter1, equalTo(aValue))
    }
}
