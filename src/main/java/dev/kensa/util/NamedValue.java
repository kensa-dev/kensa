package dev.kensa.util;

public class NamedValue {
    private final String name;
    private final Object value;

    public NamedValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public Object value() {
        return value;
    }
}
