package dev.kensa.util;

public class NameValuePair {
    private final String name;
    private final Object value;

    public NameValuePair(String name, Object value) {
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
