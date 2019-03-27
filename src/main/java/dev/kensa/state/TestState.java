package dev.kensa.state;

public enum TestState {
    Disabled("Disabled"),
    Failed("Failed"),
    NotExecuted("Not Executed"),
    Passed("Passed");

    private final String description;

    TestState(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public TestState overallStateFrom(TestState otherState) {
        if(this == Failed || this == Disabled || otherState == NotExecuted || otherState == Disabled) {
            return this;
        }

        if(otherState == Failed || otherState == Passed) {
            return otherState;
        }

        return this;
    }
}
