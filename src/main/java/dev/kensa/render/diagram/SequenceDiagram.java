package dev.kensa.render.diagram;

public class SequenceDiagram {

    private final String svg;

    public SequenceDiagram(String svg) {this.svg = svg;}

    @Override
    public String toString() {
        return svg;
    }
}
