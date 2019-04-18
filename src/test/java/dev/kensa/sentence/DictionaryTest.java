package dev.kensa.sentence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


class DictionaryTest {

    @AfterEach
    void tearDown() {
        Dictionary.clearAcronyms();
    }

    @Test
    void canPutAcronyms() {
        Dictionary.putAcronym("foo");
        Dictionary.putAcronyms("boo", "moo");

        assertThat(Dictionary.acronyms()).containsAll(List.of("foo", "boo", "moo"));
    }

    @Test
    void canPutKeywords() {
        Dictionary.putKeyword("foo");
        Dictionary.putKeywords("boo", "moo");

        assertThat(Dictionary.keywords()).containsAll(List.of("foo", "boo", "moo"));
    }

    @Test
    void canClearAcronyms() {
        Dictionary.putAcronym("foo");

        Dictionary.clearAcronyms();

        assertThat(Dictionary.acronyms()).isEmpty();
    }

    @Test
    void returnsNoMatchPatternWhenNoAcronyms() {
        assertThat(Dictionary.acronymPattern().pattern()).isEqualTo(".^");
    }

    @Test
    void returnsCorrectAcronymPattern() {
        Dictionary.putAcronym("foo");
        assertThat(Dictionary.acronymPattern().pattern()).isEqualTo("foo");

        Dictionary.putAcronym("boo");
        assertThat(Dictionary.acronymPattern().pattern()).isEqualTo("foo|boo");
    }

    @Test
    void returnsCorrectKeywordPattern() {
        Dictionary.putKeyword("foo");

        String pattern = Dictionary.keywordPattern().pattern();
        assertSoftly(softly -> {
            softly.assertThat(pattern).startsWith("^(");
            softly.assertThat(pattern).endsWith(")");
            softly.assertThat(pattern).contains("foo");
        });
    }

    @ParameterizedTest
    @NullAndEmptySource
    void throwsOnAttemptToAddEmptyOrNullKeyword(String keyword) {
        assertThatThrownBy(() -> Dictionary.putKeyword(keyword))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Dictionary.putKeywords("foo", keyword))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void throwsOnAttemptToAddEmptyOrNullAcronym(String keyword) {
        assertThatThrownBy(() -> Dictionary.putAcronym(keyword))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Dictionary.putAcronyms("foo", keyword))
                .isInstanceOf(IllegalArgumentException.class);
    }
}