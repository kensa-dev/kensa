package dev.kensa.sentence.scanner

import dev.kensa.sentence.TemplateToken.Type.Acronym
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

internal class IndexTest {
    @Test
    internal fun canTestForCancels() {
        indexOf(0, 4).cancels(indexOf(1, 3)).shouldBeTrue() // Surrounds
        indexOf(0, 4).cancels(indexOf(0, 4)).shouldBeTrue() // Same
        indexOf(0, 3).cancels(indexOf(1, 5)).shouldBeTrue() // Overlap
        indexOf(0, 4).cancels(indexOf(5, 7)).shouldBeFalse() // Disjoint
        indexOf(1, 3).cancels(indexOf(0, 4)).shouldBeFalse() // Other surrounds
        indexOf(1, 5).cancels(indexOf(0, 3)).shouldBeFalse() // Other overlaps with priority (earlier)
        indexOf(0, 2).cancels(indexOf(2, 4)).shouldBeFalse()
    }

    private fun indexOf(start: Int, end: Int) = Index(Acronym, start, end)
}