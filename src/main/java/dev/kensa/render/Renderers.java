package dev.kensa.render;

import dev.kensa.util.KensaMap;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class Renderers {

    private final SortedMap<Class<?>, Renderer<?>> renderers = new TreeMap<>(new SubclassFirstComparator());

    public <T> void add(Class<T> klass, Renderer<? extends T> renderer) {
        renderers.put(klass, renderer);
    }

    @SuppressWarnings("unchecked")
    public <T> Renderer<T> rendererFor(Class<T> klass) {
        return renderers.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().isAssignableFrom(klass))
                        .map(Map.Entry::getValue)
                        .map(renderer -> (Renderer<T>) renderer)
                        .findFirst()
                        .orElse(objectRenderer());
    }

    public Renderer rendererFor(KensaMap.Entry entry) {
        return rendererFor(entry.value().getClass());
    }

    private <T> Renderer<T> objectRenderer() {
        return value -> value == null ? "NULL" : value.toString();
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
