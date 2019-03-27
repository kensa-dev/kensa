package dev.kensa.state;

import dev.kensa.util.KensaMap;

@SuppressWarnings("WeakerAccess")
public class CapturedInteractions extends KensaMap<CapturedInteractions> {

    public void capture(CapturedInteractionBuilder builder) {
        builder.applyTo(this);
    }
}
