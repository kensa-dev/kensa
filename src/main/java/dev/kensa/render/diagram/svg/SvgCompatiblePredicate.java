package dev.kensa.render.diagram.svg;

import dev.kensa.util.KensaMap;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static dev.kensa.render.diagram.svg.Patterns.MESSAGE_KEY;
import static java.util.Arrays.asList;

public class SvgCompatiblePredicate implements Predicate<KensaMap.Entry> {
    public static Predicate<KensaMap.Entry> isSvgCompatible() {
        return new SvgCompatiblePredicate();
    }

    private final static List<Pattern> valuePatterns = asList(
            Pattern.compile("^\\.\\.\\..*\\.\\.\\.$"),
            Pattern.compile("^==.*==$")
    );

    @Override
    public boolean test(KensaMap.Entry entry) {
        return MESSAGE_KEY.matcher(entry.getKey().toLowerCase()).find()
                || valuePatterns.stream().anyMatch(p -> p.matcher(String.valueOf(entry.getValue())).find());
    }
}
