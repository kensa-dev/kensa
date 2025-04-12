package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat

interface KotlinInterface {

    fun interfaceTestMethod() {
        assertThat("abc").contains("a")
    }
}