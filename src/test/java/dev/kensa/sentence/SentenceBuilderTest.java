package dev.kensa.sentence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.kensa.sentence.SentenceTokens.*;
import static org.assertj.core.api.Assertions.assertThat;

class SentenceBuilderTest {

    private SentenceBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new SentenceBuilder();
    }

    @Test
    void canConstructASentenceFromVariousValueTypes() {
        builder.append("givenFooBar")
               .appendLiteral("literal1")
               .appendStringLiteral("stringLiteral1")
               .appendNewLine()
               .appendIdentifier("parameter1");

        assertThat(builder.build().stream())
                .containsExactly(
                        aKeywordOf("Given"),
                        aWordOf("foo"),
                        aWordOf("bar"),
                        aLiteralOf("literal1"),
                        aStringLiteralOf("stringLiteral1"),
                        aNewline(),
                        anIdentifierOf("parameter1")
                );
    }
}