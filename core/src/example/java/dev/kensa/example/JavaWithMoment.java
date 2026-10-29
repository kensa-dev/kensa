package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.Moment;

public class JavaWithMoment {

    private final Moment theTriggerWasSent = new Moment();

    private final String theColour = "red";

    void markedAction() {
        whenever(someAction().marking(theTriggerWasSent));
    }

    void markingWithoutAMoment() {
        whenever(aBrush().marking(theColour));
    }

    private Action<Object> someAction() {
        return context -> {
        };
    }

    private Brush aBrush() {
        return new Brush();
    }

    private void whenever(Action<Object> action) {
    }

    static class Brush {
        Action<Object> marking(String colour) {
            return context -> {
            };
        }
    }
}
