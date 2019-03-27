package dev.kensa.parse;

class MethodCriteria {
    private final String methodName;
    private final Class<?>[] parameterTypes;

    MethodCriteria(String methodName, Class<?>[] parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    String methodName() {
        return methodName;
    }

    Class<?>[] parameterTypes() {
        return parameterTypes;
    }
}
