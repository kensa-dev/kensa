package dev.kensa.sentence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.kensa.sentence.SentenceTokens.*;
import static org.assertj.core.api.Assertions.assertThat;

class SentenceBuilderTest {

    private SentenceBuilder builder;
    private Dictionary dictionary;

    @BeforeEach
    void setUp() {
        dictionary = new Dictionary();
        builder = new SentenceBuilder(1, Set.of("highlighted"), dictionary);
    }

    @Test
    void canConstructASentenceFromVariousValueTypes() {
        dictionary.putAcronyms(Acronym.of("FOO", ""));

        builder.append("givenFOOBar")
               .appendLiteral("literal1")
               .markLineNumber(2)
               .appendStringLiteral("stringLiteral1")
               .appendIdentifier("parameter1")
               .appendIdentifier("highlighted");

        assertThat(builder.build().stream())
                .containsExactly(
                        aKeywordOf("Given"),
                        anAcronymOf("FOO"),
                        aWordOf("bar"),
                        aLiteralOf("literal1"),
                        aNewline(),
                        aStringLiteralOf("stringLiteral1"),
                        anIdentifierOf("parameter1"),
                        aHighlightedIdentifierOf("highlighted")
                );
    }
}