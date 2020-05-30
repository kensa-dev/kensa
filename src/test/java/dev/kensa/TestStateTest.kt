package dev.kensa

import dev.kensa.state.TestState.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TestStateTest {
    @Test
    internal fun canDeriveOverallState() {
        assertThat(Passed.overallStateFrom(Failed)).isEqualTo(Failed)
        assertThat(Failed.overallStateFrom(Passed)).isEqualTo(Failed)
        assertThat(NotExecuted.overallStateFrom(Failed)).isEqualTo(Failed)
        assertThat(NotExecuted.overallStateFrom(Passed)).isEqualTo(Passed)

        // Following should not change overall state
        assertThat(Failed.overallStateFrom(Failed)).isEqualTo(Failed)
        assertThat(Passed.overallStateFrom(Passed)).isEqualTo(Passed)
        assertThat(NotExecuted.overallStateFrom(NotExecuted)).isEqualTo(NotExecuted)
        assertThat(NotExecuted.overallStateFrom(Disabled)).isEqualTo(NotExecuted)
        assertThat(Disabled.overallStateFrom(Disabled)).isEqualTo(Disabled)
        assertThat(Disabled.overallStateFrom(Failed)).isEqualTo(Disabled)
        assertThat(Disabled.overallStateFrom(Passed)).isEqualTo(Disabled)
    }
}