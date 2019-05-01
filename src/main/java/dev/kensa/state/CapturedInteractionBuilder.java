package dev.kensa.state;

import dev.kensa.util.Attributes;

public final class CapturedInteractionBuilder {

    private static final String BASIC_TEMPLATE = "%s from %s to %s";
    private static final String TEMPLATE_WITH_SEQUENCE_NUMBER = "%s %s from %s to %s";

    public static CapturedInteractionBuilder from(Party party) {
        return new CapturedInteractionBuilder(party);
    }

    private Attributes attributes = Attributes.of();
    private Party toParty;
    private String group;
    private Object content;
    private String contentDescriptor;
    private final Party fromParty;

    private CapturedInteractionBuilder(Party party) {
        fromParty = party;
    }


    public CapturedInteractionBuilder to(Party toParty) {
        this.toParty = toParty;

        return this;
    }

    public CapturedInteractionBuilder group(String group) {
        this.group = group;

        return this;
    }

    public CapturedInteractionBuilder underTest(boolean isUnderTest) {
        if (group == null & isUnderTest) {
            group = "Test";
        }

        return this;
    }

    public CapturedInteractionBuilder with(Object content, String contentDescriptor) {
        this.content = content;
        this.contentDescriptor = contentDescriptor;

        return this;
    }

    public CapturedInteractionBuilder with(Attributes attributes) {
        this.attributes = attributes;

        return this;
    }

    void applyTo(CapturedInteractions capturedInteractions) {
        String keyPrefix = group == null ? "" : String.format("(%s) ", group);

        String template = BASIC_TEMPLATE;
        String key = keyPrefix + String.format(template, contentDescriptor, fromParty.asString(), toParty.asString());

        int sequence = 1;
        while (capturedInteractions.containsKey(key)) {
            template = keyPrefix + TEMPLATE_WITH_SEQUENCE_NUMBER;
            key = String.format(template, contentDescriptor, sequence++, fromParty.asString(), toParty.asString());
        }

        capturedInteractions.put(key, content, attributes);
    }
}
