package dev.kensa.render.diagram.directive;

import java.util.List;

import static dev.kensa.util.WrappingCollector.wrapping;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class UmlBox implements UmlDirective {
    public static UmlBox surroundingBox(UmlParticipant... participants) {
        return new UmlBox(participants);
    }

    public static UmlBox surroundingBox(String title, UmlParticipant... participants) {
        return new UmlBox(participants).withTitle(title);
    }

    private final List<UmlParticipant> participants;
    private final String title;
    private final String colour;

    private UmlBox(UmlParticipant... participants) {
        this(asList(participants));
    }

    private UmlBox(List<UmlParticipant> participants) {
        this(participants, "", "");
    }

    private UmlBox(List<UmlParticipant> participants, String title, String colour) {
        this.participants = participants;
        this.title = title;
        this.colour = colour;
    }

    public UmlBox withTitle(String title) {
        return new UmlBox(participants, title, this.colour);
    }

    public UmlBox withColour(String colour) {
        return new UmlBox(participants, this.title, colour);
    }

    @Override
    public List<String> asUml() {
        return participants.stream().flatMap(p ->p.asUml().stream()).collect(wrapping(buildTitle(), "end box"));
    }

    private String buildTitle() {
        return format("box %s %s", title.isEmpty() ? "" : "\"" + title + "\"", colour).replaceAll("\\s+", " ").trim();
    }

}
