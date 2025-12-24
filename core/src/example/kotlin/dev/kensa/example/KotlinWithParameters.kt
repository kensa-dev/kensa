package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat

class KotlinWithParameters {
    fun parameterizedTest(first: String?, second: Int?) {
        /// Foopy
        /// Doopy
        /// Moopy
        assertThat(first).isIn("a", "b") /// Foopy
    }
}