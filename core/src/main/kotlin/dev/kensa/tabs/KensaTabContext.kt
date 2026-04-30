package dev.kensa.tabs

import dev.kensa.attachments.Attachments
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import java.nio.file.Path

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
    val attachments: Attachments,
    val services: KensaTabServices,
    val outputDir: Path,
    val sourceId: String = ""
)