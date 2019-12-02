package dev.kensa.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dev.kensa.util.Attributes.emptyAttributes;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class KensaMap<M extends KensaMap<M>> {

    private static final Pattern KEY_PATTERN = Pattern.compile("(?<prefix>.*)(?<key>__(?<prekey>[ ]*)idx(?<postkey>[ ]*)__)(?<suffix>.*)");

    private final Object lock = new Object();
    private final Map<String, Entry> values = new LinkedHashMap<>();

    public M put(Object value) {
        Objects.requireNonNull(value);

        return putWithUniqueKey(value.getClass().getSimpleName() + "__idx__", value, emptyAttributes());
    }

    public M put(String key, Object value) {
        return this.put(key, value, emptyAttributes());
    }

    public M put(String key, Object value, Attributes attributes) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Objects.requireNonNull(attributes);

        synchronized (lock) {
            values.put(key, new Entry(key, value, attributes));
        }

        return self();
    }

    public M putWithUniqueKey(String key, Object value, Attributes attributes) {
        Objects.requireNonNull(value);

        Matcher matcher = KEY_PATTERN.matcher(key);
        if (matcher.matches()) {
            synchronized (lock) {
                String realKey = baseKeyFrom(matcher);
                if (values.containsKey(realKey)) {
                    int i = 1;
                    do {
                        realKey = indexedKeyFrom(matcher, i++);
                    } while (values.containsKey(realKey));
                }

                values.put(realKey, new Entry(realKey, value, attributes));

                return self();
            }
        } else {
            throw new IllegalArgumentException("Must specify __key__ placeholder");
        }
    }

    public <T> void putAll(Collection<T> values) {
        Objects.requireNonNull(values);

        synchronized (lock) {
            values.forEach(this::put);
        }
    }

    public void putNamedValues(Collection<NamedValue> values) {
        Objects.requireNonNull(values);

        synchronized (lock) {
            values.forEach(nv -> this.put(nv.name(), nv.value()));
        }
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

    private M self() {
        return (M) this;
    }

    private String baseKeyFrom(Matcher matcher) {
        return matcher.group("prefix") + matcher.group("suffix");
    }

    private String indexedKeyFrom(Matcher matcher, int index) {
        return matcher.group("prefix") + (matcher.group("prekey") + index +
                (Strings.isNotBlank(matcher.group("suffix")) ? matcher.group("postkey") : ""))
                + matcher.group("suffix");
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