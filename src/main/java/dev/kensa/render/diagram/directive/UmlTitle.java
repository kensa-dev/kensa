package dev.kensa.render.diagram.directive;

import java.util.ArrayList;
import java.util.List;

import static dev.kensa.util.WrappingCollector.wrapping;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class UmlTitle implements UmlDirective {

    public static UmlTitle title(String title) {
        return new UmlTitle(title);
    }

    public static UmlTitle title(String title, String... otherLines) {
        return new UmlTitle(title, otherLines);
    }

    private final List<String> titleLines;

    private UmlTitle(String title) {
        this.titleLines = singletonList(title);
    }

    private UmlTitle(String title, String... otherLines) {
        this.titleLines = new ArrayList<>();
        this.titleLines.add(title);
        this.titleLines.addAll(asList(otherLines));
    }

    @Override
    public List<String> asUml() {
        return titleLines.stream().collect(wrapping("title", "end title"));
    }
}
