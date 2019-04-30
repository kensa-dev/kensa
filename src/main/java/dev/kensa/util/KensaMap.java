package dev.kensa.util;

import java.util.*;
import java.util.stream.Stream;

import static dev.kensa.util.Attributes.emptyAttributes;
import static java.util.Collections.synchronizedMap;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class KensaMap<M extends KensaMap<M>> {

    private final Map<String, Entry> values = synchronizedMap(new LinkedHashMap<>());

    public M put(Object value) {
        Objects.requireNonNull(value);

        String key = defaultKeyFor(value);
        values.put(key, new Entry(key, value));

        return self();
    }

    public M put(String key, Object value, Attributes attributes) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(attributes);

        values.put(key, new Entry(key, value, attributes));

        return self();
    }

    public M put(String key, Object value) {
        return this.put(key, value, emptyAttributes());
    }

    public <T> void putAll(Collection<T> values) {
        Objects.requireNonNull(values);

        values.forEach(this::put);
    }

    public void putNamedValues(Collection<NamedValue> values) {
        Objects.requireNonNull(values);

        values.forEach(nv -> this.put(nv.name(), nv.value()));
    }

    public <T> T get(String key, Class<T> clazz) {
        Entry entry = values.get(key);
        if (entry == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(entry.value.getClass())) {
            throw new ClassCastException(String.format("Cannot cast actual type [%s] to requested type [%s]", entry.getClass().getName(), clazz.getName()));
        }

        return (T) entry.value;
    }

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public Set<Entry> entrySet() {
        return new LinkedHashSet<>(values.values());
    }

    private String defaultKeyFor(Object value) {
        return keyFrom(value.getClass().getSimpleName());
    }

    private String keyFrom(String value) {
        int index = 1;
        String key = value;

        while (values.containsKey(key)) {
            key = value + " " + index++;
        }

        return key;
    }

    private M self() {
        return (M) this;
    }

    public static class Entry {
        private final String key;
        private final Object value;
        private final Attributes attributes;

        public Entry(String key, Object value, Attributes attributes) {
            this.key = key;
            this.value = value;
            this.attributes = attributes;
        }

        public Entry(String key, Object value) {
            this(key, value, emptyAttributes());
        }

        public String key() {
            return key;
        }

        public Object value() {
            return value;
        }

        public Stream<NamedValue> attributes() {
            return attributes.stream();
        }
    }
}