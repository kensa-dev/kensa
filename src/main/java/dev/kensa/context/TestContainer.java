package dev.kensa.context;

import dev.kensa.state.TestInvocationData;
import dev.kensa.state.TestState;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.kensa.state.TestState.NotExecuted;

public class TestContainer {

    private final Class<?> testClass;
    private final String displayName;
    private final Map<Method, TestInvocationData> invocationData;

    public TestContainer(Class<?> testClass, String displayName, Map<Method, TestInvocationData> invocationData) {
        this.testClass = testClass;
        this.displayName = displayName;
        this.invocationData = invocationData;
    }

    public Class<?> testClass() {
        return testClass;
    }

    public String displayName() {
        return displayName;
    }

    public TestState state() {
        TestState overallState = NotExecuted;
        for (TestInvocationData invocationData : invocationData.values()) {
            overallState = overallState.overallStateFrom(invocationData.state());
        }
        return overallState;
    }

    public Stream<TestInvocationData> invocationData() {
        return invocationData.values().stream();
    }

    public TestInvocationData invocationDataFor(Method testMethod) {
        return invocationData.get(testMethod);
    }

    public <T> T transform(Function<TestContainer, T> transformer) {
        return transformer.apply(this);
    }
}
