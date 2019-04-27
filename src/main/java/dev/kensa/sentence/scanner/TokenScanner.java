package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Dictionary;
import dev.kensa.sentence.Token;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.kensa.sentence.Token.Type.*;
import static java.lang.Integer.compare;
import static java.util.stream.Collectors.joining;

public class TokenScanner {
    private static final Pattern NO_MATCH_PATTERN = Pattern.compile(".^");
    private final Pattern highlightPattern;

    public TokenScanner(Set<String> highlightedValues) {
        highlightPattern = highlightedValues.isEmpty() ? NO_MATCH_PATTERN :
                Pattern.compile(highlightedValues.stream().sorted((a1, a2) -> compare(a2.length(), a1.length())) // ** Important: Longest first
                                                 .collect(joining("|")));
    }

    public Indices scan(String string) {
        Indices indices = new Indices();

        scanFor(Keyword, Dictionary.keywordPattern(), string, indices);
        scanFor(Acronym, Dictionary.acronymPattern(), string, indices);
        scanFor(HighlightedWord, highlightPattern, string, indices);
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
        String[] splitWords = segment.split("(?=\\p{Lu})");
        for (String word : splitWords) {
            words.add(new Index(Word, offset + segmentOffset, offset + segmentOffset + word.length()));
            segmentOffset += word.length();
        }
    }

    private void scanFor(Token.Type type, Pattern pattern, String string, Indices indices) {
        Matcher matcher = pattern.matcher(string);
        int start = 0;
        while (matcher.find(start)) {
            start = matcher.end();
            indices.put(type, matcher.start(), matcher.end());
        }
    }
}