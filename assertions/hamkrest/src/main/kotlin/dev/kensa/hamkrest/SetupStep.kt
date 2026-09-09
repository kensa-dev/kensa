package dev.kensa.hamkrest

import dev.kensa.SetupScope
import dev.kensa.SetupStep

interface HamkrestSetupStep : SetupStep, WithHamkrest

interface HamkrestSetupScope : SetupScope, WithHamkrest

fun hamkrestSetupStep(block: HamkrestSetupScope.() -> Unit): HamkrestSetupStep = object : HamkrestSetupStep {
    override fun setup(scope: SetupScope) {
        val hamkrestScope = object : HamkrestSetupScope, SetupScope by scope {}
        hamkrestScope.block()
    }
}
