package dev.kensa.acceptance

import dev.kensa.Kensa.konfigure
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

internal abstract class KensaAcceptanceTest {
    companion object {
        lateinit var kensaOutputDir: Path

        @JvmStatic
        @BeforeAll
        fun beforeAll(@TempDir tempDir: Path) {
            kensaOutputDir = tempDir.resolve("kensa-output")

            konfigure {
                outputDir = kensaOutputDir
                isOutputEnabled = true
            }
        }
    }
}