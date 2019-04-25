package dev.kensa.render;

import java.util.*;
import java.util.stream.Stream;

public final class Renderers {

    private final SortedMap<Class<?>, Renderer<Object>> renderers = new TreeMap<>(new SubclassFirstComparator());

    public <T> void add(Class<T> klass, Renderer<? extends T> renderer) {
        renderers.put(klass, (Renderer<Object>) renderer);
    }

    public String renderValueOnly(Object value) {
        if (value == null) {
            return "NULL";
        }

        return render(value);
    }

    public Stream<Map.Entry<String, Object>> renderAll(Object value) {
        Map<String, Object> renderedItems = new LinkedHashMap<>();

        if (value == null) {
            renderedItems.put("value", "NULL");
        } else {
            Renderer<Object> renderer = rendererFor(value.getClass())
                    .map(r -> {
                        for (RenderableAttribute renderableAttribute : r.renderableAttributes()) {
                            Object attr = renderableAttribute.renderableFrom(value);
                            if (attr instanceof Map) {
                                renderedItems.put(renderableAttribute.name(), attr);
                            } else {
                                renderedItems.put(renderableAttribute.name(), render(attr));
                            }
                        }

                        return r;
                    })
                    .orElseGet(() -> Object::toString);

            renderedItems.put("value", renderer.render(value));
        }

        return renderedItems.entrySet().stream();
    }

    private String render(Object value) {
        return rendererFor(value.getClass())
                .map(r -> r.render(value))
                .orElse(value.toString());
    }

    private Optional<Renderer<Object>> rendererFor(Class klass) {
        return renderers.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().isAssignableFrom(klass))
                        .map(Map.Entry::getValue)
                        .findFirst();
    }

    private static class SubclassFirstComparator implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> c1, Class<?> c2) {
            if (c1 == null) {
                if (c2 == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (c2 == null) {
                return -1;
            }

            if (c1 == c2) {
                return 0;
            }

            boolean c1Sub = c2.isAssignableFrom(c1);
            boolean c2Sub = c1.isAssignableFrom(c2);

            if (c1Sub && !c2Sub) {
                return -1;
            } else if (c2Sub && !c1Sub) {
                return 1;
            }

            return c1.getName().compareTo(c2.getName());
        }
    }
}
