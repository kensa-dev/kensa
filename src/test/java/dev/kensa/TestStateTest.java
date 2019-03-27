package dev.kensa;

import org.junit.jupiter.api.Test;

import static dev.kensa.state.TestState.*;
import static org.assertj.core.api.Assertions.assertThat;

class TestStateTest {

    @Test
    void canDeriveOverallState() {
        assertThat(Passed.overallStateFrom(Failed)).isEqualTo(Failed);
        assertThat(Failed.overallStateFrom(Passed)).isEqualTo(Failed);
        assertThat(NotExecuted.overallStateFrom(Failed)).isEqualTo(Failed);
        assertThat(NotExecuted.overallStateFrom(Passed)).isEqualTo(Passed);

        // Following should not change overall state
        assertThat(Failed.overallStateFrom(Failed)).isEqualTo(Failed);
        assertThat(Passed.overallStateFrom(Passed)).isEqualTo(Passed);
        assertThat(NotExecuted.overallStateFrom(NotExecuted)).isEqualTo(NotExecuted);
        assertThat(NotExecuted.overallStateFrom(Disabled)).isEqualTo(NotExecuted);
        assertThat(Disabled.overallStateFrom(Disabled)).isEqualTo(Disabled);
        assertThat(Disabled.overallStateFrom(Failed)).isEqualTo(Disabled);
        assertThat(Disabled.overallStateFrom(Passed)).isEqualTo(Disabled);
    }
}