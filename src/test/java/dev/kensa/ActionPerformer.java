package dev.kensa;

public class ActionPerformer {
    public String perform(String actionName) {
        return String.format("Performed: %s", actionName);
    }
}
