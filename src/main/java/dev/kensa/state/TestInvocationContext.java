package dev.kensa.state;

import java.lang.reflect.Method;

public class TestInvocationContext {
    private final Object testInstance;
    private final Method testMethod;
    private final Object[] testParameters;

    public TestInvocationContext(Object testInstance, Method testMethod, Object[] testParameters) {
        this.testInstance = testInstance;
        this.testMethod = testMethod;
        this.testParameters = testParameters;
    }

    public Object testInstance() {
        return testInstance;
    }

    public Method testMethod() {
        return testMethod;
    }

    public Object[] testParameters() {
        return testParameters;
    }
}
