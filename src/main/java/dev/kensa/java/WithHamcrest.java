package dev.kensa.java;

import dev.kensa.StateExtractor;
import dev.kensa.context.HamcrestThen;
import org.hamcrest.Matcher;

import java.time.temporal.ChronoUnit;

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

    default <T> void thenEventually(Long timeout, ChronoUnit timeUnit, StateExtractor<T> extractor, Matcher<? super T> matcher) {
        HamcrestThen.thenEventually(timeout, timeUnit, testContext(), extractor, matcher);
    }
}
