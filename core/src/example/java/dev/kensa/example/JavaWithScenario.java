package dev.kensa.example;

import dev.kensa.Resolve;

public class JavaWithScenario {

    @Resolve
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