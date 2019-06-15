package dev.kensa.sentence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DictionaryTest {

    private Dictionary dictionary;

    @BeforeEach
    void setUp() {
        dictionary = new Dictionary();
    }

    @AfterEach
    void tearDown() {
        dictionary.clearAcronyms();
    }

    @Test
    void canStreamExistingAcronyms() {
        Acronym[] acronyms = {
                Acronym.of("foo", "means foo"),
                Acronym.of("boo", "means boo"),
                Acronym.of("moo", "means moo")
        };

        dictionary.putAcronyms(acronyms);

        assertThat(dictionary.acronyms()).containsAll(asList(acronyms));
    }

    @Test
    void canPutKeywords() {
        dictionary.putKeyword("foo");
        dictionary.putKeywords("boo", "moo");

        assertThat(dictionary.keywords()).containsAll(List.of("foo", "boo", "moo"));
    }

    @Test
    void canClearAcronyms() {
        dictionary.putAcronyms(Acronym.of("foo", "foo"));

        dictionary.clearAcronyms();

        assertThat(dictionary.acronyms()).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void throwsOnAttemptToAddEmptyOrNullKeyword(String keyword) {
        assertThatThrownBy(() -> dictionary.putKeyword(keyword))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> dictionary.putKeywords("foo", keyword))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsOnAttemptToAddEmptyOrNullAcronym() {
        assertThatThrownBy(() -> dictionary.putAcronyms((Acronym[]) null))
                .isInstanceOf(NullPointerException.class);
    }
}