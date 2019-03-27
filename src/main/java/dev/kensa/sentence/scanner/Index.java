package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Token;

public class Index {
    private final Token.Type type;
    private final int start;
    private final int end;

    Index(Token.Type type, int start, int end) {
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public Token.Type type() {
        return type;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    boolean cancels(Index other) {
        return start <= other.start && end > other.start;
    }

    @Override
    public String toString() {
        return "Index{" +
                "type=" + type +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
