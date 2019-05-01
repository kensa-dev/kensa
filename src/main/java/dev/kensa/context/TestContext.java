package dev.kensa.context;

import dev.kensa.ActionUnderTest;
import dev.kensa.GivensBuilder;
import dev.kensa.GivensWithInteractionsBuilder;
import dev.kensa.StateExtractor;
import dev.kensa.state.CapturedInteractions;
import dev.kensa.state.Givens;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

public class TestContext {

    private final Givens givens;
    private final CapturedInteractions interactions;

    public TestContext(Givens givens, CapturedInteractions interactions) {
        this.givens = givens;
        this.interactions = interactions;
    }

    public void given(GivensWithInteractionsBuilder builder) {
        builder.build(givens, interactions);
    }

    public void given(GivensBuilder builder) {
        builder.build(givens);
    }

    public void when(ActionUnderTest action) {
        try {
            interactions.setUnderTest(true);
            action.execute(givens, interactions);
        } finally {
            interactions.setUnderTest(false);
        }
    }

    public <T> ObjectAssert<T> then(StateExtractor<T> extractor) {
        try {
            interactions.setUnderTest(true);
            return Assertions.assertThat(extractor.execute(interactions));
        } finally {
            interactions.setUnderTest(false);
        }
    }

    public <T> void then(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        try {
            interactions.setUnderTest(true);
            MatcherAssert.assertThat(extractor.execute(interactions), matcher);
        } finally {
            interactions.setUnderTest(false);
        }
    }

    public void disableInteractionTestGroup() {
        interactions.disableUnderTest();
    }

    public Givens givens() {
        return givens;
    }

    public CapturedInteractions interactions() {
        return interactions;
    }
}
