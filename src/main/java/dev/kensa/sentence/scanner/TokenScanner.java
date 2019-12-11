package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Token;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static dev.kensa.sentence.Token.Type.*;

public class TokenScanner {

    private static final Pattern CAMEL_CASE_SPLITTER = Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[0-9])|(?<=[0-9])(?=[A-Z])");

    private final Set<String> highlightedValues;
    private final Set<String> keywords;
    private final Set<String> acronyms;

    public TokenScanner(Set<String> highlightedValues, Set<String> keywords, Set<String> acronyms) {
        this.highlightedValues = highlightedValues;
        this.keywords = keywords;
        this.acronyms = acronyms;
    }

    public Indices scan(String string) {
        Indices indices = new Indices();

        scanForKeywords(string, indices);
        scanForAcronyms(string, indices);
        scanForWords(string, indices);

        return indices;
    }

    private void scanForWords(String string, Indices indices) {
        Set<Index> wordList = new HashSet<>();

        int lastIndexEnd = 0;
        for (Index thisIndex : indices) {
            if (lastIndexEnd < thisIndex.start()) {
                splitIntoWords(wordList, string.substring(lastIndexEnd, thisIndex.start()), lastIndexEnd);
            }

            lastIndexEnd = thisIndex.end();
        }

        if (lastIndexEnd < string.length()) {
            splitIntoWords(wordList, string.substring(lastIndexEnd), lastIndexEnd);
        }

        indices.putWords(wordList);
    }

    private void splitIntoWords(Set<Index> words, String segment, int offset) {
        int segmentOffset = 0;
        for (String word : camelCaseSplit(segment)) {
            words.add(new Index(typeOf(word), offset + segmentOffset, offset + segmentOffset + word.length()));
            segmentOffset += word.length();
        }
    }

    private Token.Type typeOf(String word) {
        return highlightedValues.contains(word) ? HighlightedWord : Word;
    }

    private void scanForAcronyms(String string, Indices indices) {
        for (String word : camelCaseSplit(string)) {
            if (acronyms.stream()
                        .anyMatch(a -> a.equalsIgnoreCase(word))) {
                indices.put(Acronym, string.indexOf(word), string.indexOf(word) + word.length());
            }
        }
    }

    private void scanForKeywords(String string, Indices indices) {
        String[] strings = camelCaseSplit(string);
        String word = strings[0];
        if (keywords.stream()
                    .anyMatch(a -> a.equalsIgnoreCase(word))) {
            indices.put(Keyword, string.indexOf(word), string.indexOf(word) + word.length());
        }
    }

    private String[] camelCaseSplit(String string) {
        return CAMEL_CASE_SPLITTER.split(string);
    }
}