package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Token;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Indices implements Iterable<Index> {

    private final SortedSet<Index> indices = new TreeSet<>((i1, i2) -> {
        if (i1.start() == i2.start()) {
            return Integer.compare(i2.end(), i1.end()); // Take the largest end index first
        }

        return Integer.compare(i1.start(), i2.start());
    });

    void put(Token.Type type, int start, int end) {
        indices.add(new Index(type, start, end));

        Index lastIndex = null;
        for (Iterator<Index> iterator = indices.iterator(); iterator.hasNext(); ) {
            Index thisIndex = iterator.next();

            if (lastIndex != null) {
                if (lastIndex.cancels(thisIndex)) {
                    iterator.remove();
                } else {
                    lastIndex = thisIndex;
                }
            } else {
                lastIndex = thisIndex;
            }
        }
    }

    void putWords(Set<Index> words) {
        indices.addAll(words);
    }

    @Override
    public Iterator<Index> iterator() {
        return indices.iterator();
    }

    public Stream<Index> stream() {
        return indices.stream();
    }

    @Override
    public String toString() {
        return "Indices{" +
                "indices=" + indices +
                '}';
    }
}
