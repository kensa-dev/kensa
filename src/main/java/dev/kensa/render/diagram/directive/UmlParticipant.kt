package dev.kensa.render.diagram.directive;

import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class UmlParticipant implements UmlDirective {
    public static UmlParticipant participant(String name) {
        return new UmlParticipant("participant", name);
    }

    public static UmlParticipant actor(String name) {
        return new UmlParticipant("actor", name);
    }

    public static UmlParticipant boundary(String name) {
        return new UmlParticipant("boundary", name);
    }

    public static UmlParticipant control(String name) {
        return new UmlParticipant("control", name);
    }

    public static UmlParticipant entity(String name) {
        return new UmlParticipant("entity", name);
    }

    public static UmlParticipant database(String name) {
        return new UmlParticipant("database", name);
    }

    public static UmlParticipant collections(String name) {
        return new UmlParticipant("collections", name);
    }

    private final String type;
    private final String name;
    private final String colour;
    private final String alias;

    private UmlParticipant(String type, String name) {
        this(type, name, "", "");
    }

    private UmlParticipant(String type, String name, String colour, String alias) {
        this.type = type;
        this.name = name;
        this.colour = colour;
        this.alias = alias;
    }

    public UmlParticipant withColour(String colour) {
        return new UmlParticipant(type, name, colour, alias);
    }

    public UmlParticipant withAlias(String alias) {
        return new UmlParticipant(type, name, colour, alias);
    }

    @Override
    public List<String> asUml() {
        return singletonList(format("%s %s %s %s", type, name, fmtAlias(alias), colour).replaceAll("\\s+", " ").trim());
    }

    private String fmtAlias(String alias) {
        return alias.isEmpty() ? "" : "as \"" + alias + "\"";
    }
}