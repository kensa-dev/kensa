package dev.kensa.hints;

public class JavaFieldWithPathHint<T> {
    private final String path;

    public JavaFieldWithPathHint(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public JavaFieldWithPathHint<T> of(T value) {
        return this;
    }
}