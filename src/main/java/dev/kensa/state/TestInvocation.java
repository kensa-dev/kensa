package dev.kensa.state;

import dev.kensa.parse.ParsedTest;
import dev.kensa.render.diagram.SequenceDiagram;
import dev.kensa.sentence.Acronym;
import dev.kensa.sentence.Sentence;
import dev.kensa.sentence.Sentences;
import dev.kensa.util.KensaMap;
import dev.kensa.util.NamedValue;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TestInvocation {
    private final Duration elapsed;
    private final Sentences sentences;
    private final Collection<NamedValue> parameters;
    private final Collection<NamedValue> highlightedFields;
    private final TestState state;
    private final Throwable executionException;
    private final Givens givens;
    private final CapturedInteractions interactions;
    private final Set<Acronym> acronyms;
    private final SequenceDiagram sequenceDiagram;

    public TestInvocation(
            Duration elapsed,
            ParsedTest parsedTest,
            Givens givens,
            CapturedInteractions interactions,
            Set<Acronym> acronyms,
            Throwable executionException,
            SequenceDiagram sequenceDiagram
    ) {
        this.elapsed = elapsed;
        this.sentences = parsedTest.sentences();
        this.parameters = parsedTest.parameters();
        this.highlightedFields = parsedTest.highlightedFields();
        this.givens = givens;
        this.interactions = interactions;
        this.executionException = executionException;
        this.state = executionException == null ? TestState.Passed : TestState.Failed;
        this.acronyms = acronyms;
        this.sequenceDiagram = sequenceDiagram;

        this.givens.putNamedValues(highlightedFields);
    }

    public Duration elapsed() {
        return elapsed;
    }

    public Stream<Sentence> sentences() {
        return sentences.stream();
    }

    public Collection<NamedValue> parameters() {
        return parameters;
    }

    public Stream<NamedValue> highlightedFields() {
        return highlightedFields.stream();
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

    public Stream<Acronym> acronyms() {
        return acronyms.stream();
    }

    public SequenceDiagram sequenceDiagram() {
        return sequenceDiagram;
    }
}
