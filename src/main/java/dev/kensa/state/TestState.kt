package dev.kensa.state

import java.util.*

enum class TestState(val description: String) {
    Disabled("Disabled"),
    Failed("Failed"),
    NotExecuted("Not Executed"),
    Passed("Passed");

    fun overallStateFrom(otherState: TestState): TestState =
            if (DISABLED_FAILED.contains(this) || DISABLED_NOT_EXECUTED.contains(otherState)) {
                this
            } else if (FAILED_PASSED.contains(otherState)) {
                otherState
            } else {
                this
            }

    companion object {
        val DISABLED_FAILED: EnumSet<TestState> = EnumSet.of(Disabled, Failed)
        val DISABLED_NOT_EXECUTED: EnumSet<TestState> = EnumSet.of(Disabled, NotExecuted)
        val FAILED_PASSED: EnumSet<TestState> = EnumSet.of(Failed, Passed)
    }
}