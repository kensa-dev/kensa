package dev.kensa.sentence;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Sentences implements Iterable<Sentence> {
    private final List<Sentence> sentences;

    public Sentences(List<Sentence> sentences) {this.sentences = sentences;}

    @Override
    public Iterator<Sentence> iterator() {
        return sentences.iterator();
    }

    public Stream<Sentence> stream() {
        return sentences.stream();
    }
}
