package dev.kensa.render;

public interface RenderableAttribute<T, RA> {
    String name();

    RA renderableFrom(T value);
}
