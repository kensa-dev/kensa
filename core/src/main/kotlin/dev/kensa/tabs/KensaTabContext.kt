package dev.kensa.tabs

import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs

data class KensaTabContext(
    val tabId: String,
    val tabName: String,
    val invocationIdentifier: String?,
    val testClass: String,
    val testMethod: String,
    val invocationIndex: Int,
    val invocationDisplayName: String,
    val invocationState: String,
    val fixtures: Fixtures,
    val capturedOutputs: CapturedOutputs,
    val services: KensaTabServices
)