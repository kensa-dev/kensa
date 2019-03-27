package dev.kensa.parse;

import dev.kensa.sentence.Sentence;
import dev.kensa.sentence.Sentences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.Collections.emptySet;

class SentenceCollector implements Collector<Sentence, List<Sentence>, Sentences> {

    static SentenceCollector asSentences() {
        return new SentenceCollector();
    }

    private SentenceCollector() {
    }

    @Override
    public Supplier<List<Sentence>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<Sentence>, Sentence> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<Sentence>> combiner() {
        return (sentences, sentences2) -> {
            sentences.addAll(sentences2);
            return sentences;
        };
    }

    @Override
    public Function<List<Sentence>, Sentences> finisher() {
        return Sentences::new;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return emptySet();
    }
}
