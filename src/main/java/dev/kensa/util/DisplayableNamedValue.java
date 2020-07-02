package dev.kensa.util;

public class DisplayableNamedValue {
    private final String name;
    private final String displayName;
    private final Object value;

    public DisplayableNamedValue(String name, String displayName, Object value) {
        this.name = name;
        this.displayName = displayName;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String displayName() {
        return displayName;
    }

    public Object value() {
        return value;
    }
}
