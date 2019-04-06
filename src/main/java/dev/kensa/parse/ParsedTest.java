package dev.kensa.parse;

import dev.kensa.sentence.Sentences;
import dev.kensa.util.NameValuePair;

import java.util.Collection;

public class ParsedTest {
    private final Collection<NameValuePair> parameters;
    private final Sentences sentences;

    public ParsedTest(Collection<NameValuePair> parameters, Sentences sentences) {
        this.parameters = parameters;
        this.sentences = sentences;
    }

    public Collection<NameValuePair> parameters() {
        return parameters;
    }

    public Sentences sentences() {
        return sentences;
    }
}
