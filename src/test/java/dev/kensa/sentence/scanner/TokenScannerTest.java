package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Token;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static dev.kensa.sentence.Token.Type.HighlightedWord;
import static dev.kensa.sentence.Token.Type.Word;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class TokenScannerTest {

    private Pattern noMatchPattern = Pattern.compile(".^");

    @Test
    void recognisesKeywordsAtStartOfSentenceOnly() {
        var string = "givenSomethingWasGivenWhenThen";

        Indices indices = tokenScannerWith(noMatchPattern).scan(string);

        indices.stream()
               .filter(index -> index.type() == Token.Type.Keyword)
               .forEach(index -> assertThat(index.start()).isEqualTo(0));
    }

    @Test
    void scansSimpleStringWithNoAcronyms() {
        List<String> expected = List.of("given", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(noMatchPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithSingleAcronymInMiddle() {
        Pattern acronymPattern = Pattern.compile("FTTC|FTTP|TT|BT");

        List<String> expected = List.of("given", "FTTC", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    // Kensa#6
    @Test
    void scansStringWithSingleCharacterFollowedByAcronym() {
        Pattern acronymPattern = Pattern.compile("FTTC");

        List<String> expected = List.of("a", "FTTC", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }
    @Test
    void scansStringWithSingleAcronymAtStart() {
        Pattern acronymPattern = Pattern.compile("FTTC|FTTP|TT|BT");

        List<String> expected = List.of("FTTC", "given", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithSingleAcronymAtEnd() {
        Pattern acronymPattern = Pattern.compile("FTTC|FTTP|TT|BT");

        List<String> expected = List.of("given", "And", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithMultipleAcronyms() {
        Pattern acronymPattern = Pattern.compile("FTTC|FTTP|TT|BT");

        List<String> expected = List.of("BT", "given", "And", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void choosesLongestMatchingAcronym() {
        Pattern acronymPattern = Pattern.compile("FTTC|FTTP|FT|BT");

        List<String> expected = List.of("BT", "given", "FT", "And", "FTTP", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithMultipleRepeatingAcronyms() {
        Pattern acronymPattern = Pattern.compile("FTTC|FTTP|TT|BT");

        List<String> expected = List.of("BT", "given", "BT", "And", "FTTP", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithRepeatingAcronyms() {
        Pattern acronymPattern = Pattern.compile("FTTC|FTTP|TT|BT");

        List<String> expected = List.of("BT", "BT");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronymPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithHighlightedWords() {
        List<String> expected = List.of("An", "Important", "Priority", "Thing", "Notification");

        var string = String.join("", expected);

        Indices indices = tokenScannerWith(noMatchPattern).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
        assertThat(indices.stream()).extracting(Index::type).containsExactly(Word, HighlightedWord, HighlightedWord, Word, Word);
    }

    private List<String> transformed(Indices indices, String string) {
        return indices.stream()
                      .map(index -> string.substring(index.start(), index.end()))
                      .collect(toList());
    }

    private TokenScanner tokenScannerWith(Pattern acronymPattern) {
        return new TokenScanner(Set.of("Important", "Priority", "No"), noMatchPattern, acronymPattern);
    }
}