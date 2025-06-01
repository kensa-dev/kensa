package dev.kensa.example

import dev.kensa.Highlight
import dev.kensa.Resolve
import dev.kensa.DummyAssert.Companion.assertThat

class KotlinWithInterface : KotlinInterface {
    private val field1: String? = null

    @field:Resolve
    private val field2: String? = null

    @field:Highlight
    @field:Resolve
    private val field3: String? = null

    fun simpleTest() {
        assertThat("xyz").contains("x")
    }
}