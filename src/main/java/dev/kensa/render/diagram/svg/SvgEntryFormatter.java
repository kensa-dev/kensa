package dev.kensa.render.diagram.svg;

import dev.kensa.util.KensaMap;

import java.util.function.Function;
import java.util.regex.Matcher;

import static dev.kensa.render.diagram.svg.Patterns.MESSAGE_KEY;

public class SvgEntryFormatter implements Function<KensaMap.Entry, String> {

    public static Function<KensaMap.Entry, String> toSvgFormat() {
        return new SvgEntryFormatter();
    }

    @Override
    public String apply(KensaMap.Entry entry) {
        Matcher matcher = MESSAGE_KEY.matcher(entry.key());
        if (matcher.matches()) {
            String interactionId = String.valueOf(entry.key().hashCode());
            String interactionName = matcher.group(1).trim();
            String from = matcher.group(2).trim();
            String to = matcher.group(3).trim();
            return String.format("%s ->> %s:<text class=sequence_diagram_clickable sequence_diagram_interaction_id=%s>%s</text>", from, to, interactionId, interactionName);
        } else {
            return String.valueOf(entry.value()).trim();
        }
    }
}
