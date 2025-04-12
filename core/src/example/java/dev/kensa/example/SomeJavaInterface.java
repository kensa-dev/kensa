package dev.kensa.example;

public interface SomeJavaInterface {
    default String aDefaultMethod() {
        return "DefaultValue";
    }

    String overrideMe();

    String renderMe();
}
