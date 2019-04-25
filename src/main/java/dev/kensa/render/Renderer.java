package dev.kensa.render;

import java.util.Set;

import static java.util.Collections.emptySet;

public interface Renderer<T> {
    String render(T value);

    default Set<RenderableAttribute<T, ?>> renderableAttributes() {
        return emptySet();
    }
}
