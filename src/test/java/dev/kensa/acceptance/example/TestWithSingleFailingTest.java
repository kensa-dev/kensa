package dev.kensa.acceptance.example;

import dev.kensa.java.JavaKensaTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWithSingleFailingTest implements JavaKensaTest {
    @Test
    void failingTest() {
        assertThat(true).isFalse();
    }
}
