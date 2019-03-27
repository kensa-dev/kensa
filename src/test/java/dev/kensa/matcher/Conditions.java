package dev.kensa.matcher;

import org.assertj.core.api.Condition;

import java.util.function.Function;
import java.util.function.Predicate;

public final class Conditions {

    public static <T, EXPECTED> Condition<T> equalTo(String description, EXPECTED expected, Predicate<T> predicate) {
        return new Condition<>(
                predicate,
                String.format("a %s of %%s", description),
                expected
        );
    }

    public static <T, EXPECTED> Condition<T> equalTo(EXPECTED expected, Predicate<T> predicate) {
        return new Condition<>(
                predicate,
                String.format("a %s of %%s", unCamelClassOf(expected)),
                expected
        );
    }

    public static <T, EXPECTED> Condition<T> equalTo(Function<T, EXPECTED> extractor, EXPECTED expected) {
        return equalTo(expected, Predicates.equalTo(extractor, expected));
    }

    public static <T, EXPECTED> Condition<T> equalTo(String description, Function<T, EXPECTED> extractor, EXPECTED expected) {
        return equalTo(description, expected, Predicates.equalTo(extractor, expected));
    }

    public static <T, EXPECTED> Condition<T> matches(Function<T, EXPECTED> extractor, Condition<EXPECTED> subCondition, EXPECTED expected) {
        return new Condition<>(
                t -> subCondition.matches(extractor.apply(t)),
                String.format("a %s matching %%s", unCamelClassOf(expected)),
                expected
        );
    }

    private static String unCamel(String camelCasedWords) {
        return String.join(" ", camelCasedWords.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])"));
    }

    private static String unCamel(Class<?> c) {
        return unCamel(c.getSimpleName());
    }

    private static String unCamelClassOf(Object o) {
        return o == null ? "Null Instance" : unCamel(o.getClass().getSimpleName());
    }

    private Conditions() {
    }
}
