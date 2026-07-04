package dev.kensa.example;

import dev.kensa.RenderedValue;

public class JavaWithReplaceSentence {

    @RenderedValue
    private final String trackingId = "TRK-001";

    public void replacedWithPlainWords() {
        /*+ ReplaceSentence: given a simple replacement */
        given(() -> {
        });
    }

    public void replacedWithFieldInterpolation() {
        /*+ ReplaceSentence: given the tracking id is {trackingId} */
        given(() -> {
        });
    }

    public void replacedAndSubsequentStatementsUnaffected() {
        /*+ ReplaceSentence: given a simple replacement */
        given(() -> {
        });

        when(() -> {
        });
    }

    private void given(Runnable block) {
        block.run();
    }

    private void when(Runnable block) {
        block.run();
    }
}
