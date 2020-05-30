package dev.kensa.java;

import dev.kensa.StateExtractor;
import org.hamcrest.Matcher;

import static dev.kensa.context.TestContextHolder.testContext;

public interface WithHamcrest {
    default <T> void then(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        testContext().then(extractor, matcher);
    }

    default <T> void and(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        then(extractor, matcher);
    }
}
