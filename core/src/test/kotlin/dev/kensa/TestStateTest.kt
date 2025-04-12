package dev.kensa

import dev.kensa.state.TestState.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class TestStateTest {
    @Test
    internal fun canDeriveOverallState() {
        Passed.overallStateFrom(Failed) shouldBe Failed
        Failed.overallStateFrom(Passed) shouldBe Failed
        NotExecuted.overallStateFrom(Failed) shouldBe Failed
        NotExecuted.overallStateFrom(Passed) shouldBe Passed

        // Following should not change overall state
        Failed.overallStateFrom(Failed) shouldBe Failed
        Passed.overallStateFrom(Passed) shouldBe Passed
        NotExecuted.overallStateFrom(NotExecuted) shouldBe NotExecuted
        NotExecuted.overallStateFrom(Disabled) shouldBe NotExecuted
        Disabled.overallStateFrom(Disabled) shouldBe Disabled
        Disabled.overallStateFrom(Failed) shouldBe Disabled
        Disabled.overallStateFrom(Passed) shouldBe Disabled
    }
}