package dev.kensa.parse;

import dev.kensa.sentence.Sentences;
import dev.kensa.util.NamedValue;

import java.util.Collection;
import java.util.Set;

public class ParsedTest {
    private final Collection<NamedValue> parameters;
    private final Sentences sentences;
    private final Collection<NamedValue> highlightedFields;

    public ParsedTest(Collection<NamedValue> parameters, Sentences sentences, Set<NamedValue> highlightedFields) {
        this.parameters = parameters;
        this.sentences = sentences;
        this.highlightedFields = highlightedFields;
    }

    public Collection<NamedValue> parameters() {
        return parameters;
    }

    public Sentences sentences() {
        return sentences;
    }

    public Collection<NamedValue> highlightedFields() {
        return highlightedFields;
    }
}
