package dev.kensa.acceptance.example;

import dev.kensa.KensaTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWithSingleFailingTest implements KensaTest {
    @Test
    void failingTest() {
        assertThat(true).isFalse();
    }
}
