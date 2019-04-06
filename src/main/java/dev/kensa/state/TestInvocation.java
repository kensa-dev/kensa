package dev.kensa.state;

import dev.kensa.parse.ParsedTest;
import dev.kensa.render.diagram.SequenceDiagram;
import dev.kensa.sentence.Sentence;
import dev.kensa.sentence.Sentences;
import dev.kensa.util.KensaMap;
import dev.kensa.util.NameValuePair;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TestInvocation {
    private final Duration elapsed;
    private final Sentences sentences;
    private final Collection<NameValuePair> parameters;
    private final TestState state;
    private final Throwable executionException;
    private final Givens givens;
    private final CapturedInteractions interactions;
    private final List<String> highlightValues;
    private final List<String> acronyms;
    private final SequenceDiagram sequenceDiagram;

    public TestInvocation(
            Duration elapsed,
            ParsedTest parsedTest,
            Givens givens,
            CapturedInteractions interactions,
            List<String> highlightValues,
            List<String> acronyms,
            Throwable executionException,
            SequenceDiagram sequenceDiagram
    ) {
        this.elapsed = elapsed;
        this.sentences = parsedTest.sentences();
        this.parameters = parsedTest.parameters();
        this.givens = givens;
        this.interactions = interactions;
        this.executionException = executionException;
        this.state = executionException == null ? TestState.Passed : TestState.Failed;
        this.highlightValues = highlightValues;
        this.acronyms = acronyms;
        this.sequenceDiagram = sequenceDiagram;
    }

    public Duration elapsed() {
        return elapsed;
    }

    public Collection<NameValuePair> parameters() {
        return parameters;
    }

    public Stream<Sentence> sentences() {
        return sentences.stream();
    }

    public TestState state() {
        return state;
    }

    public Stream<KensaMap.Entry> givens() {
        return givens.entrySet().stream();
    }

    public Stream<KensaMap.Entry> interactions() {
        return interactions.entrySet().stream();
    }

    public Optional<Throwable> executionException() {
        return Optional.ofNullable(executionException);
    }

    public Stream<String> highlights() {
        return highlightValues.stream();
    }

    public Stream<String> acronyms() {
        return acronyms.stream();
    }

    public SequenceDiagram sequenceDiagram() {
        return sequenceDiagram;
    }
}
