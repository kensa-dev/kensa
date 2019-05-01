package dev.kensa;

import org.assertj.core.api.ObjectAssert;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.kensa.context.TestContextHolder.testContext;

@ExtendWith(KensaExtension.class)
public interface KensaTest {

    default void disableInteractionTestGroup() {
        testContext().disableInteractionTestGroup();
    }

    default void given(GivensBuilder builder) {
        testContext().given(builder);
    }

    default void and(GivensBuilder builder) {
        given(builder);
    }

    default void given(GivensWithInteractionsBuilder builder) {
        testContext().given(builder);
    }

    default void and(GivensWithInteractionsBuilder builder) {
        given(builder);
    }

    default void when(ActionUnderTest action) {
        testContext().when(action);
    }

    default <T> ObjectAssert<T> then(StateExtractor<T> extractor) {
        return testContext().then(extractor);
    }

    default <T> void then(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        testContext().then(extractor, matcher);
    }

    default <T> ObjectAssert<T> and(StateExtractor<T> extractor) {
        return then(extractor);
    }

    default <T> void and(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        then(extractor, matcher);
    }
}
