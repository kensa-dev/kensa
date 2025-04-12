package dev.kensa

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.Kensa.configure
import dev.kensa.extension.TestParameterResolver
import dev.kensa.extension.TestParameterResolver.Companion.MY_PARAMETER_VALUE
import dev.kensa.extension.TestParameterResolver.MyArgument
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(TestParameterResolver::class)
class KotlinWithParameterResolverExtensionAndParameterizedTest : KensaTest, WithHamkrest {
    @SentenceValue
    private val aValue = "aStringValue"

    @BeforeEach
    fun setUp() {
        configure()
            .withValueRenderer(MyArgument::class.java, MyArgumentRenderer)
    }

    @ParameterizedTest
    @ValueSource(strings = ["arg1", "arg2"])
    fun theTest(@SentenceValue parameter1: String, parameter2: MyArgument) {
        assertThat(parameter2.value, equalTo(MY_PARAMETER_VALUE))
        assertThat(listOf("arg1", "arg2"), hasItem(parameter1))
    }

    private fun hasItem(expected: String) = Matcher(List<String>::contains, expected)

}
