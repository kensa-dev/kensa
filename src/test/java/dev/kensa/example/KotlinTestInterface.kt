package dev.kensa.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

interface KotlinTestInterface {

    @Test
    fun interfaceTestMethod() {
        assertThat("abc").contains("a")
    }
}