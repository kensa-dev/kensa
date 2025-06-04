package dev.kensa.example;

import dev.kensa.RenderedValue;

public class JavaWithScenario {

    @RenderedValue
    private MyJavaScenario scenario;

    private Integer foo = 666;

    void usingAScenario() {
        doSomething(scenario.foo());
    }

    private void doSomething(Integer param) {
    }
}

class MyJavaScenario {
    Integer foo() {
        return 666;
    }
}