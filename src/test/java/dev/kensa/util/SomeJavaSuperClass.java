package dev.kensa.util;

public class SomeJavaSuperClass {
    private final Integer superField;

    public SomeJavaSuperClass(Integer superField) {this.superField = superField;}

    private Integer aSuperMethod() {
        return superField;
    }

    public Integer superRenderMe() {
        return superField;
    }
}
