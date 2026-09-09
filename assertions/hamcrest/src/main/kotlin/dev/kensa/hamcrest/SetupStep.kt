package dev.kensa.hamcrest

import dev.kensa.SetupScope
import dev.kensa.SetupStep

interface HamcrestSetupStep : SetupStep, WithHamcrest

interface HamcrestSetupScope : SetupScope, WithHamcrest

fun hamcrestSetupStep(block: HamcrestSetupScope.() -> Unit): HamcrestSetupStep = object : HamcrestSetupStep {
    override fun setup(scope: SetupScope) {
        val hamcrestScope = object : HamcrestSetupScope, SetupScope by scope {}
        hamcrestScope.block()
    }
}
