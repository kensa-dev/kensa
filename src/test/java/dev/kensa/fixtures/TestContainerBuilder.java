package dev.kensa.fixtures;

import dev.kensa.context.TestContainer;
import dev.kensa.state.TestInvocationData;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class TestContainerBuilder {

    private Instant executionTime = Instant.now();
    private Class<?> testClass = Object.class;
    private String displayName = "Display Name";
    private Map<Method, TestInvocationData> invocationMap = new HashMap<>();

    public TestContainerBuilder withExecutionTime(Instant executionTime) {
        this.executionTime = executionTime;

        return this;
    }

    public TestContainerBuilder withTestClass(Class<?> testClass) {
        this.testClass = testClass;

        return this;
    }

    public TestContainerBuilder withDisplayName(String displayName) {
        this.displayName = displayName;

        return this;
    }

    public TestContainerBuilder withInvocation(Method method, TestInvocationData invocation) {
        invocationMap.put(method, invocation);

        return this;
    }

    public TestContainer build() {
        return new TestContainer(
                testClass,
                displayName,
                invocationMap
        );
    }
}
