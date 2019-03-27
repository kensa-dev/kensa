package dev.kensa.context;

import dev.kensa.ActionUnderTest;
import dev.kensa.GivensBuilder;
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

    public void given(GivensBuilder builder) {
        builder.build(givens);
    }

    public void when(ActionUnderTest action) {
        action.execute(givens, interactions);
    }

    public <T> ObjectAssert<T> then(StateExtractor<T> extractor) {
        return Assertions.assertThat(extractor.execute(interactions));
    }

    public <T> void then(StateExtractor<T> extractor, Matcher<? super T> matcher) {
        MatcherAssert.assertThat(extractor.execute(interactions), matcher);
    }
}
