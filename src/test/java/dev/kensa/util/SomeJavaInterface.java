package dev.kensa.util;

public interface SomeJavaInterface {
    default String aDefaultMethod() {
        return "DefaultValue";
    }

    String overrideMe();

    String renderMe();
}
