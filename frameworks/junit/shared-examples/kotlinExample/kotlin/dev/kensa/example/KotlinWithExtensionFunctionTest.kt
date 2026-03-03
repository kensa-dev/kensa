package dev.kensa.example

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.RenderedValue
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

class KotlinWithExtensionFunctionTest : KotlinExampleTest(), WithHamkrest {

    val value: String = "true"

    @Test
    fun simpleTest() {
        assertThat(value.extensionFunction(), equalTo("true"))
        assertThat(value.extensionFunction1("false"), equalTo("false"))
    }

    @RenderedValue
    fun String.extensionFunction(): String {
        return this
    }

    @RenderedValue
    fun String.extensionFunction1(value: String): String {
        return value
    }

}