package dev.kensa.sentence;

import java.util.Objects;

public class SentenceToken implements Token {

    private final Type type;
    private final String value;

    public SentenceToken(Type type, String value) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);

        this.type = type;
        this.value = value;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String asString() {
        return value;
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
