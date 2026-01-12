package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.RenderedValue

class KotlinWithExtensionFunction {

    val value: String = "true"

    fun simpleTest() {
        assertThat(value.extensionFunction()).isEqualTo("true")
    }

    @RenderedValue
    fun String.extensionFunction(): String {
        return this
    }

}