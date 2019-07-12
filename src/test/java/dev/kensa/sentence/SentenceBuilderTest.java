package dev.kensa.sentence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.kensa.sentence.SentenceTokens.*;
import static org.assertj.core.api.Assertions.assertThat;

class SentenceBuilderTest {

    private SentenceBuilder builder;

    @BeforeEach
    void setUp() {
        Dictionary dictionary = new Dictionary();
        dictionary.putAcronyms(simpleAcronymOf("FOO"), simpleAcronymOf("BAR"), simpleAcronymOf("LA1"), simpleAcronymOf("HA1"));

        builder = new SentenceBuilder(1, Set.of("highlighted", "HA1"), dictionary.keywords(), dictionary.acronymStrings());
    }

    @Test
    void canConstructASentenceFromVariousValueTypes() {
        builder.append("givenFOOMooBar")
               .appendLiteral("literal1")
               .appendStringLiteral("LA1")
               .appendIdentifier("HA1")
               .markLineNumber(2)
               .appendStringLiteral("stringLiteral1")
               .appendIdentifier("parameter1")
               .appendIdentifier("highlighted");

        assertThat(builder.build().stream())
                .containsExactly(
                        aKeywordOf("Given"),
                        anAcronymOf("FOO"),
                        aWordOf("moo"),
                        anAcronymOf("BAR"),
                        aLiteralOf("literal1"),
                        aStringLiteralAcronymOf("LA1"),
                        aHighlightedAcronymOf("HA1"),
                        aNewline(),
                        aStringLiteralOf("stringLiteral1"),
                        anIdentifierOf("parameter1"),
                        aHighlightedIdentifierOf("highlighted")
                );
    }

    private Acronym simpleAcronymOf(String acronym) {
        return Acronym.of(acronym, "");
    }
}