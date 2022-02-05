package dev.kensa.acceptance.example

import dev.kensa.Highlight
import dev.kensa.Scenario
import dev.kensa.SentenceValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinTestFromInterface : KotlinTestInterface {
    private val field1: String? = null

    @field:Scenario
    private val field2: String? = null

    @field:Highlight @field:SentenceValue
    private val field3: String? = null

    @Test
    fun classTestMethod() {
        assertThat("xyz").contains("x")
    }
}