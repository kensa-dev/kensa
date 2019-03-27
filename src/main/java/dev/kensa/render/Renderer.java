package dev.kensa.render;

public interface Renderer<T> {
    String render(T value);
}
