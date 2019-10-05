package dev.kensa.util;

import java.util.regex.Pattern;

public final class Strings {

    private static Pattern CAMEL_SPLIT_PATTERN = Pattern.compile(
            String.format(
                    "%s|%s|%s",
                    "(?<=[A-Z])(?=[A-Z][a-z])",     // Uppercase behind, Uppercase then Lowercase ahead
                    "(?<=[^A-Z])(?=[A-Z])",         // Non-Uppercase behind, Uppercase ahead
                    "(?<=[A-Za-z])(?=[0-9])"        // Letter behind, number ahead
            )
    );

    public static String unCamel(String phrase) {
        if (isBlank(phrase)) {
            return phrase;
        }

        phrase = phrase.replaceAll("[^a-zA-Z0-9]", "");

        String[] elements = CAMEL_SPLIT_PATTERN.split(phrase);
        elements[0] = Character.toUpperCase(elements[0].charAt(0)) + elements[0].substring(1);

        return String.join(" ", elements);
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isBlank(String value) {
        if (value == null || value.length() == 0) {
            return true;
        }

        return !value.chars()
                     .filter(c -> !Character.isWhitespace(c))
                     .findFirst()
                     .isPresent();
    }

    private Strings() {
    }
}
