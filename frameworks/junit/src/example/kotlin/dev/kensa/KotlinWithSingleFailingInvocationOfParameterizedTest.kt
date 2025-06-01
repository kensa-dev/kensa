package dev.kensa

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinWithSingleFailingInvocationOfParameterizedTest : KensaTest, WithHamkrest {
    @Resolve
    private val aValue = "correct"

    @ParameterizedTest
    @ValueSource(strings = ["correct", "wrong", "correct"])
    fun theTest(parameter1: String) {
        assertThat(parameter1, equalTo(aValue))
    }
}
