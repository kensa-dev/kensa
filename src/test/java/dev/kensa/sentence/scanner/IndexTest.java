package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Token;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndexTest {

    @Test
    void canTestForCancels() {
        assertThat(indexOf(0, 4).cancels(indexOf(1, 3))).isTrue(); // Surrounds
        assertThat(indexOf(0, 4).cancels(indexOf(0, 4))).isTrue(); // Same
        assertThat(indexOf(0, 3).cancels(indexOf(1, 5))).isTrue(); // Overlap

        assertThat(indexOf(0, 4).cancels(indexOf(5, 7))).isFalse(); // Disjoint
        assertThat(indexOf(1, 3).cancels(indexOf(0, 4))).isFalse(); // Other surrounds
        assertThat(indexOf(1, 5).cancels(indexOf(0, 3))).isFalse(); // Other overlaps with priority (earlier)
        assertThat(indexOf(0, 2).cancels(indexOf(2, 4))).isFalse();
    }

    private Index indexOf(int start, int end) {
        return new Index(Token.Type.Acronym, start, end);
    }
}