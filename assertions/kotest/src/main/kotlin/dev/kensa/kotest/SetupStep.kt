package dev.kensa.kotest

import dev.kensa.SetupScope
import dev.kensa.SetupStep

interface KotestSetupStep : SetupStep, WithKotest

interface KotestSetupScope : SetupScope, WithKotest

fun kotestSetupStep(block: KotestSetupScope.() -> Unit): KotestSetupStep = object : KotestSetupStep {
    override fun setup(scope: SetupScope) {
        val kotestScope = object : KotestSetupScope, SetupScope by scope {}
        kotestScope.block()
    }
}
