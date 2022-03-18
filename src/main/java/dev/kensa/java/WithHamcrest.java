package dev.kensa.java;

import dev.kensa.StateExtractor;
import dev.kensa.context.HamcrestThen;
import org.hamcrest.Matcher;

import static dev.kensa.context.TestContextHolder.testContext;

public interface WithHamcrest {
    default <T> void then(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        HamcrestThen.then(testContext(), extractor, matcher);
    }

    default <T> void and(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        then(extractor, matcher);
    }

    default <T> void thenEventually(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        HamcrestThen.thenEventually(testContext(), extractor, matcher);
    }
}
