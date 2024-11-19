package dev.kensa.example;

import dev.kensa.Scenario;
import dev.kensa.StateExtractor;
import dev.kensa.java.JavaKensaTest;
import dev.kensa.java.WithAssertJ;
import org.junit.jupiter.api.Test;

public class JavaTestWithScenario implements JavaKensaTest, WithAssertJ {

    @Scenario
    private MyJavaScenario scenario;

    private Integer foo = 666;

    @Test
    void useTheScenario() {
        then(testFoo())
                .isEqualTo(scenario.foo())
                .isEqualTo(foo)
        ;
    }

    private StateExtractor<Integer> testFoo() {
        return interactions -> 666;
    }
}

class MyJavaScenario {
    Integer foo() {
        return 666;
    }
}