package dev.kensa.sentence.scanner;

import dev.kensa.sentence.Token;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.kensa.sentence.Token.Type.*;

public class TokenScanner {

    private final Set<String> highlightedValues;
    private final Pattern keywordPattern;
    private final Pattern acronymPattern;

    public TokenScanner(Set<String> highlightedValues, Pattern keywordPattern, Pattern acronymPattern) {
        this.highlightedValues = highlightedValues;
        this.keywordPattern = keywordPattern;
        this.acronymPattern = acronymPattern;
    }

    public Indices scan(String string) {
        Indices indices = new Indices();

        scanFor(Keyword, keywordPattern, string, indices);
        scanFor(Acronym, acronymPattern, string, indices);
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
            words.add(new Index(typeOf(word), offset + segmentOffset, offset + segmentOffset + word.length()));
            segmentOffset += word.length();
        }
    }

    private Token.Type typeOf(String word) {
        return highlightedValues.contains(word) ? HighlightedWord : Word;
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