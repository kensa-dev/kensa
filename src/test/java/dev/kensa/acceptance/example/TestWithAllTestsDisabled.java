package dev.kensa.acceptance.example;

import dev.kensa.KensaTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestWithAllTestsDisabled implements KensaTest {

    @Disabled
    @Test
    void test1() {
    }

    @Disabled
    @Test
    @DisplayName("This is test 2")
    void test2() {
    }
}
