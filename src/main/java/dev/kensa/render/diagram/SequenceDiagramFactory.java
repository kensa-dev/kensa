package dev.kensa.render.diagram;

import dev.kensa.render.diagram.directive.UmlDirective;
import dev.kensa.state.CapturedInteractions;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static dev.kensa.render.diagram.GroupedMessageCollector.toGroupedMessages;
import static dev.kensa.render.diagram.svg.SvgCompatiblePredicate.isSvgCompatible;
import static dev.kensa.render.diagram.svg.SvgEntryFormatter.toSvgFormat;
import static java.util.stream.Collectors.joining;
import static net.sourceforge.plantuml.FileFormat.SVG;

public class SequenceDiagramFactory {

    private final List<UmlDirective> directives;

    public SequenceDiagramFactory(List<UmlDirective> directives) {
        this.directives = directives;
    }

    public SequenceDiagram create(CapturedInteractions interactions) {
        return createSvg(
                Stream.concat(
                        participants(),
                        buildEventsFrom(interactions).stream()
                )
                      .collect(joining("\n", "@startuml\n", "\n@enduml"))
        );
    }

    private Stream<String> participants() {
        return directives.stream().flatMap(uml -> uml.asUml().stream());
    }

    private static List<String> buildEventsFrom(CapturedInteractions interactions) {
        return interactions.entrySet()
                           .stream()
                           .filter(isSvgCompatible())
                           .map(toSvgFormat())
                           .collect(toGroupedMessages());
    }

    private static SequenceDiagram createSvg(String plantUmlMarkup) {
        SourceStringReader reader = new SourceStringReader(plantUmlMarkup);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            reader.generateImage(os, new FileFormatOption(SVG));
            return new SequenceDiagram(new String(os.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
