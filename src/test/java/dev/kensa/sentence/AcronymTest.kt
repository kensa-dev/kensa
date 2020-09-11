package dev.kensa.sentence

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class AcronymTest {
    @Test
    internal fun throwsExceptionWhenAcronymIsTooShort() {
        assertThatThrownBy { Acronym.of("A", "Too Short") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    internal fun createsShortAcronym() {
        assertThat( Acronym.of("AB", "Just Right").meaning).isEqualTo("Just Right")
    }
}
