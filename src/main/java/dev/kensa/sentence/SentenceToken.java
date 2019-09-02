package dev.kensa.sentence;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class SentenceToken implements Token {

    private final Type type;
    private final String value;
    private final List<SentenceToken> tokens;

    public SentenceToken(String value, List<SentenceToken> tokens) {
        Objects.requireNonNull(value);

        this.type = Type.Expandable;
        this.value = value;
        this.tokens = tokens;
    }

    public SentenceToken(Type type, String value) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);

        this.type = type;
        this.value = value;
        this.tokens = emptyList();
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String value() {
        return value;
    }

    public Stream<SentenceToken> tokens() {
        return tokens.stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SentenceToken that = (SentenceToken) o;
        return type == that.type &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "SentenceToken{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
