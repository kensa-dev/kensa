package dev.kensa

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.Kensa.configure
import dev.kensa.extension.TestParameterResolver
import dev.kensa.extension.TestParameterResolver.Companion.MY_PARAMETER_VALUE
import dev.kensa.extension.TestParameterResolver.MyArgument
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestParameterResolver::class)
class KotlinWithParameterResolverExtensionTest : KensaTest, WithHamkrest {
    @RenderedValue
    private val aValue = "aStringValue"

    @BeforeEach
    fun setUp() {
        configure()
            .withValueRenderer(MyArgument::class.java, MyArgumentRenderer)
    }

    @Test
    fun theTest(parameter: MyArgument) {
        assertThat(parameter.value, equalTo(MY_PARAMETER_VALUE))
    }
}
