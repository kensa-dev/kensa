package dev.kensa.matcher;

import dev.kensa.sentence.Token;
import dev.kensa.sentence.scanner.Index;
import org.assertj.core.api.Condition;

import static dev.kensa.matcher.Conditions.equalTo;
import static org.assertj.core.api.Assertions.allOf;

public final class IndexMatcher {

    public static Condition<Index> theSameAs(Index expected) {
        return allOf(
                aTypeOf(expected.type()),
                aStartOf(expected.start()),
                anEndOf(expected.end())
        );
    }

    public static Condition<Index> aTypeOf(Token.Type expected) {
        return equalTo("Type", Index::type, expected);
    }

    public static Condition<Index> aStartOf(int expected) {
        return equalTo("Start Index", Index::start, expected);
    }

    public static Condition<Index> anEndOf(int expected) {
        return equalTo("End Index", Index::end, expected);
    }
}
