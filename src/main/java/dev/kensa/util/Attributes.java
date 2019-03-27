package dev.kensa.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Attributes implements Iterable<NameValuePair> {

    private static final Attributes EMPTY_ATTRIBUTES = new Attributes(Set.of());

    static Attributes emptyAttributes() {
        return EMPTY_ATTRIBUTES;
    }

    public static Attributes of(String name, String value) {
        return of(Set.of(new NameValuePair(name, value)));
    }

    public static Attributes of(String name1, String value1, String name2, String value2) {
        return of(
                new NameValuePair(name1, value1),
                new NameValuePair(name2, value2)
        );
    }

    public static Attributes of(String name1, String value1, String name2, String value2, String name3, String value3) {
        return of(
                new NameValuePair(name1, value1),
                new NameValuePair(name2, value2),
                new NameValuePair(name3, value3)
        );
    }

    public static Attributes of(Set<NameValuePair> attributes) {
        return new Attributes(new LinkedHashSet<>(attributes));
    }

    public static Attributes of(NameValuePair... attributes) {
        return new Attributes(new LinkedHashSet<>(Set.of(attributes)));
    }

    private final Set<NameValuePair> attributes;

    public Attributes(Set<NameValuePair> attributes) {this.attributes = attributes;}

    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    public Stream<NameValuePair> stream() {
        return attributes.stream();
    }

    @Override
    public Iterator<NameValuePair> iterator() {
        return attributes.iterator();
    }
}
