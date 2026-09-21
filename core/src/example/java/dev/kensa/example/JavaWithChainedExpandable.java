package dev.kensa.example;

import dev.kensa.ExpandableSentence;
import dev.kensa.RenderedValue;

public class JavaWithChainedExpandable {

    @RenderedValue
    private final String aFirstName = "John";

    void chainedAfterArgumentlessExpandable() {
        assertThatHas(withTheExpectedFields().and(aFirstName));
    }

    void chainedAfterExpandableWithArguments() {
        assertThatHas(withTheExpectedFields("Mr").and(aFirstName));
    }

    @ExpandableSentence
    private Matchers withTheExpectedFields() {
        return new Matchers();
    }

    @ExpandableSentence
    private Matchers withTheExpectedFields(String aTitle) {
        return new Matchers();
    }

    private void assertThatHas(Matchers matchers) {
    }

    static class Matchers {
        Matchers and(String value) {
            return this;
        }
    }
}
