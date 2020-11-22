package dev.kensa.sentence.scanner

import dev.kensa.sentence.TokenType.Acronym
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class IndexTest {
    @Test
    internal fun canTestForCancels() {
        Assertions.assertThat(indexOf(0, 4).cancels(indexOf(1, 3))).isTrue // Surrounds
        Assertions.assertThat(indexOf(0, 4).cancels(indexOf(0, 4))).isTrue // Same
        Assertions.assertThat(indexOf(0, 3).cancels(indexOf(1, 5))).isTrue // Overlap
        Assertions.assertThat(indexOf(0, 4).cancels(indexOf(5, 7))).isFalse() // Disjoint
        Assertions.assertThat(indexOf(1, 3).cancels(indexOf(0, 4))).isFalse() // Other surrounds
        Assertions.assertThat(indexOf(1, 5).cancels(indexOf(0, 3))).isFalse() // Other overlaps with priority (earlier)
        Assertions.assertThat(indexOf(0, 2).cancels(indexOf(2, 4))).isFalse()
    }

    private fun indexOf(start: Int, end: Int) = Index(Acronym, start, end)
}