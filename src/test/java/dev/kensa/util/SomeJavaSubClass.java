package dev.kensa.util;

import java.util.function.Supplier;

public class SomeJavaSubClass extends SomeJavaSuperClass implements SomeJavaInterface {
    private String field1 = "xxx";
    private final Supplier<String> valueSupplier = () -> field1;

    public SomeJavaSubClass(Integer integer, String field1) {
        super(integer);
        this.field1 = field1;
    }

    private String aMethod() {
        return field1;
    }

    @Override
    public String overrideMe() {
        return field1;
    }

    @Override
    public String renderMe() {
        return field1;
    }
}
