package dev.kensa.state;

import dev.kensa.util.Attributes;

public final class CapturedInteractionBuilder {

    private static final String TEMPLATE = "%s __idx __from %s to %s";

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

        String key = keyPrefix + String.format(TEMPLATE, contentDescriptor, fromParty.asString(), toParty.asString());

        capturedInteractions.putWithUniqueKey(key, content, attributes);
    }
}
