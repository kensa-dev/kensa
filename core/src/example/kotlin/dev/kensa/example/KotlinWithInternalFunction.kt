package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat

class KotlinWithInternalFunction {

    internal fun simpleTest() {
        assertThat("true").isEqualTo("true")
    }
}