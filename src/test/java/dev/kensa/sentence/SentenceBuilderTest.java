package dev.kensa.sentence;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void tearDown() {
        Dictionary.clearAcronyms();
    }

    @Test
    void canConstructASentenceFromVariousValueTypes() {
        Dictionary.putAcronym("FOO");

        builder.append("givenFOOBar")
               .appendLiteral("literal1")
               .appendStringLiteral("stringLiteral1")
               .appendNewLine()
               .appendIdentifier("parameter1");

        assertThat(builder.build().stream())
                .containsExactly(
                        aKeywordOf("Given"),
                        anAcronymOf("FOO"),
                        aWordOf("bar"),
                        aLiteralOf("literal1"),
                        aStringLiteralOf("stringLiteral1"),
                        aNewline(),
                        anIdentifierOf("parameter1")
                );
    }
}