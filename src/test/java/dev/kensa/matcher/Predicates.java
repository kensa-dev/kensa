package dev.kensa.matcher;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Predicates {

    public static <T, R> Predicate<T> equalTo(Function<T, R> extractor, R expected) {
        return new ExtractingPredicateWithTest<>(extractor, expected, Objects::equals);
    }

    private static class ExtractingPredicateWithTest<T, R> implements Predicate<T> {

        private final Function<T, R> extractor;
        private final R expected;
        private final BiFunction<R, R, Boolean> test;

        private ExtractingPredicateWithTest(Function<T, R> extractor, R expected, BiFunction<R, R, Boolean> test) {
            this.extractor = extractor;
            this.expected = expected;
            this.test = test;
        }

        @Override
        public boolean test(T actual) {
            return test.apply(extractor.apply(actual), expected);
        }
    }

    private Predicates() {
    }
}
