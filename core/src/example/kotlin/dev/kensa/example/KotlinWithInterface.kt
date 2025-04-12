package dev.kensa.example

import dev.kensa.Highlight
import dev.kensa.Scenario
import dev.kensa.SentenceValue
import dev.kensa.DummyAssert.Companion.assertThat

class KotlinWithInterface : KotlinInterface {
    private val field1: String? = null

    @field:Scenario
    private val field2: String? = null

    @field:Highlight
    @field:SentenceValue
    private val field3: String? = null

    fun simpleTest() {
        assertThat("xyz").contains("x")
    }
}