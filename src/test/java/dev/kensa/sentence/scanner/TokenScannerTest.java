package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static dev.kensa.sentence.Token.Type.HighlightedWord;
import static dev.kensa.sentence.Token.Type.Word;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class TokenScannerTest {

    @Test
    void recognisesKeywordsAtStartOfSentenceOnly() {
        var string = "givenSomethingWasGivenWhenThen";

        Indices indices = tokenScannerWith(emptySet()).scan(string);

        indices.stream()
               .filter(index -> index.type() == Token.Type.Keyword)
               .forEach(index -> assertThat(index.start()).isEqualTo(0));
    }

    @Test
    void scansSimpleStringWithNoAcronyms() {
        List<String> expected = List.of("given", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(emptySet()).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithSingleAcronymInMiddle() {
        Set<String> acronyms = Set.of("FTTC", "FTTP", "TT", "BT");

        List<String> expected = List.of("given", "FTTC", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronyms).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    // Kensa#6
    @Test
    void scansStringWithSingleCharacterFollowedByAcronym() {
        Set<String> acronym = Set.of("FTTC");

        List<String> expected = List.of("a", "FTTC", "And", "This", "And", "That");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronym).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithSingleAcronymAtEnd() {
        Set<String> acronym = Set.of("FTTC", "FTTP", "TT", "BT");

        List<String> expected = List.of("given", "And", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronym).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithMultipleAcronyms() {
        Set<String> acronym = Set.of("FTTC", "FTTP", "TT", "BT");

        List<String> expected = List.of("given", "And", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronym).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void choosesLongestMatchingAcronym() {
        Set<String> acronyms = Set.of("FTTC", "FTTP", "FT", "BT");

        List<String> expected = List.of("given", "FT", "And", "FTTP", "This", "And", "That", "FTTC");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronyms).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("mixedCaseExamples")
    void scansAcronymsCorrectlyWhenMixedCase(List<String> expected) {
        Set<String> acronyms = Set.of("FTTC");

        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronyms).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    private static Stream<List<String>> mixedCaseExamples() {
        return Stream.of(
                List.of("FTTC"),
                List.of("Fttc"),
                List.of("Ftt", "C"),
                List.of("Ft", "Tc"),
                List.of("Ft", "TC"),
                List.of("F", "Ttc"),
                List.of("Ft", "Tc"),
                List.of("fttc")
        );
    }

    @Test
    void scansStringWithAcronymSpanningCamelWords() {
        Set<String> acronyms = Set.of("ONT");

        List<String> expected = List.of("a", "Notification", "Type", "Of");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronyms).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithAcronymSpanningCamelWords_afterInitialCap() {
        Set<String> acronyms = Set.of("ONT");

        List<String> expected = List.of("a", "Contact");
        var string = String.join("", expected);

        Indices indices = tokenScannerWith(acronyms).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
    }

    @Test
    void scansStringWithHighlightedWords() {
        List<String> expected = List.of("An", "Important", "Priority", "Thing", "Notification");

        var string = String.join("", expected);

        Indices indices = tokenScannerWith(emptySet()).scan(string);

        assertThat(transformed(indices, string)).isEqualTo(expected);
        assertThat(indices.stream()).extracting(Index::type).containsExactly(Word, HighlightedWord, HighlightedWord, Word, Word);
    }

    private List<String> transformed(Indices indices, String string) {
        return indices.stream()
                      .map(index -> string.substring(index.start(), index.end()))
                      .collect(toList());
    }

    private TokenScanner tokenScannerWith(Set<String> acronyms) {
        return new TokenScanner(Set.of("Important", "Priority", "No"), Set.of("Given", "When", "Then"), acronyms);
    }
}