package dev.kensa.sentence

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class AcronymTest {
    @Test
    internal fun throwsExceptionWhenAcronymIsTooShort() {
        shouldThrowExactly<IllegalArgumentException> { Acronym.of("A", "Too Short") }
    }

    @Test
    internal fun createsShortAcronym() {
        Acronym.of("AB", "Just Right").meaning shouldBe "Just Right"
    }
}
