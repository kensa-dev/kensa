package dev.kensa.render;

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
    public String render(Object value) {
        if(value == null) {
            return "NULL";
        }
        return rendererFor(value.getClass()).render(value);
    }

    private Renderer rendererFor(Class klass) {
        return renderers.entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().isAssignableFrom(klass))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(Object::toString);
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
