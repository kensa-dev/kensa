package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Dictionary;
import dev.kensa.sentence.Token;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.kensa.sentence.Token.Type.*;
import static java.util.stream.Collectors.joining;

public class TokenScanner {

    public Indices scan(String string) {
        Indices indices = new Indices();

        scanFor(keywordPattern(), string, indices, Keyword);
        scanFor(acronymPattern(), string, indices, Acronym);
        scanForWords(string, indices);

        return indices;
    }

    private void scanForWords(String string, Indices indices) {
        Set<Index> wordList = new HashSet<>();

        var lastIndexEnd = 0;
        for (Index thisIndex : indices) {
            if (lastIndexEnd < thisIndex.start() - 1) {
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

    private String acronymPattern() {
        return Dictionary.acronyms().sorted((a1, a2) -> Integer.compare(a2.length(), a1.length())) // ** Important: Longest first
                         .collect(joining("|"));
    }

    private String keywordPattern() {
        return Dictionary.keywords().sorted((a1, a2) -> Integer.compare(a2.length(), a1.length())) // ** Important: Longest first
                         .collect(joining("|", "^(", ")"));
    }

    private void scanFor(String patternString, String string, Indices indices, Token.Type type) {
        if (patternString.length() > 0) {
            Pattern pattern = Pattern.compile(patternString);

            Matcher matcher = pattern.matcher(string);
            int start = 0;
            while (matcher.find(start)) {
                start = matcher.end();
                indices.put(type, matcher.start(), matcher.end());
            }
        }
    }
}