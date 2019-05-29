package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Dictionary;
import dev.kensa.sentence.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static dev.kensa.sentence.Acronym.of;
import static dev.kensa.sentence.Token.Type.HighlightedWord;
import static dev.kensa.sentence.Token.Type.Word;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class TokenScannerTest {

    private TokenScanner tokenScanner;
    private Dictionary dictionary;

    @BeforeEach
    void setUp() {
        dictionary = new Dictionary();
        tokenScanner = new TokenScanner(Set.of("Important", "Priority", "No"), dictionary);
    }

    @Test
    void recognisesKeywordsAtStartOfSentenceOnly() {
        var string = "givenSomethingWasGivenWhenThen";

        Indices indices = tokenScanner.scan(string);

        indices.stream()
               .filter(index -> index.type() == Token.Type.Keyword)
               .forEach(index -> assertThat(index.start()).isEqualTo(0));
    }

    @Test
    void scansSimpleStringWithNoAcronyms() {
        List<String> expected = List.of("given", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithSingleAcronymInMiddle() {
        dictionary.putAcronyms(
                of("FTTC", ""),
                of("TT", ""),
                of("BT", ""),
                of("FTTP", "")
        );

        List<String> expected = List.of("given", "FTTC", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    // Kensa#6
    @Test
    void scansStringWithSingleCharacterFollowedByAcronym() {
        dictionary.putAcronyms(of("FTTC", ""));
        List<String> expected = List.of("a", "FTTC", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithSingleAcronymAtStart() {
        dictionary.putAcronyms(
                of("FTTC", ""),
                of("TT", ""),
                of("BT", ""),
                of("FTTP", "")
        );

        List<String> expected = List.of("FTTC", "given", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithSingleAcronymAtEnd() {
        dictionary.putAcronyms(
                of("FTTC", ""),
                of("TT", ""),
                of("BT", ""),
                of("FTTP", "")
        );

        List<String> expected = List.of("given", "And", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithMultipleAcronyms() {
        dictionary.putAcronyms(
                of("FTTC", ""),
                of("TT", ""),
                of("BT", ""),
                of("FTTP", "")
        );

        List<String> expected = List.of("BT", "given", "And", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void choosesLongestMatchingAcronym() {
        dictionary.putAcronyms(
                of("FTTC", ""),
                of("FT", ""),
                of("BT", ""),
                of("FTTP", "")
        );

        List<String> expected = List.of("BT", "given", "FT", "And", "FTTP", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithMultipleRepeatingAcronyms() {
        dictionary.putAcronyms(
                of("FTTC", ""),
                of("TT", ""),
                of("BT", ""),
                of("FTTP", "")
        );

        List<String> expected = List.of("BT", "given", "BT", "And", "FTTP", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithRepeatingAcronyms() {
        dictionary.putAcronyms(
                of("FTTC", ""),
                of("TT", ""),
                of("BT", ""),
                of("FTTP", "")
        );

        List<String> expected = List.of("BT", "BT");
        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithHighlightedWords() {
        List<String> expected = List.of("An", "Important", "Priority", "Thing", "Notification");

        var string = String.join("", expected);

        Indices indices = tokenScanner.scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
        assertThat(indices.stream()).extracting(Index::type).containsExactly(Word, HighlightedWord, HighlightedWord, Word, Word);
    }

    private List<String> transformed(Indices indices, String string) {
        return indices.stream()
                      .map(index -> string.substring(index.start(), index.end()))
                      .collect(toList());
    }
}