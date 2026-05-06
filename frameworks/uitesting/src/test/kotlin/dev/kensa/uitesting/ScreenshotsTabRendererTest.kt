package dev.kensa.uitesting

import dev.kensa.attachments.Attachments
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.tabs.DefaultKensaTabServices
import dev.kensa.tabs.KensaTabContext
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class ScreenshotsTabRendererTest {

    @Test
    fun `render returns null when no screenshots are attached`(@TempDir outputDir: Path) {
        val ctx = newCtx(outputDir, attachments = Attachments())
        ScreenshotsTabRenderer().render(ctx).shouldBeNull()
    }

    @Test
    fun `mediaType identifies the screenshots vendor json`() {
        ScreenshotsTabRenderer().mediaType() shouldBe "application/vnd.kensa.screenshots+json"
    }

    @Test
    fun `render writes PNGs to disk and returns JSON referencing them by relative URL`(@TempDir outputDir: Path) {
        val driver = FakeBrowserDriver()
        val screenshots = CapturedScreenshots()
        screenshots.capture(driver, "before")
        screenshots.capture(driver, "after")

        val attachments = Attachments().apply { put(SCREENSHOTS_KEY, screenshots) }
        val ctx = newCtx(outputDir, attachments = attachments, tabId = "Screenshots")

        val json = ScreenshotsTabRenderer().render(ctx)
        requireNotNull(json)

        json shouldBe """[{"label":"before","src":"tabs/FooTest/aTest/invocation-0/Screenshots-screenshots/0-before.png"},{"label":"after","src":"tabs/FooTest/aTest/invocation-0/Screenshots-screenshots/1-after.png"}]"""
        json shouldNotContain "data:image/png;base64"

        val outDir = outputDir
            .resolve("tabs")
            .resolve("FooTest")
            .resolve("aTest")
            .resolve("invocation-0")
            .resolve("Screenshots-screenshots")
        outDir.resolve("0-before.png").shouldExist()
        outDir.resolve("1-after.png").shouldExist()
    }

    @Test
    fun `render replaces traversal segments so output stays under outputDir`(@TempDir outputDir: Path) {
        val driver = FakeBrowserDriver()
        val screenshots = CapturedScreenshots().apply { capture(driver, "shot") }
        val attachments = Attachments().apply { put(SCREENSHOTS_KEY, screenshots) }
        val ctx = newCtx(outputDir, attachments = attachments, tabId = "..")

        ScreenshotsTabRenderer().render(ctx)

        val expectedDir = outputDir
            .resolve("tabs")
            .resolve("FooTest")
            .resolve("aTest")
            .resolve("invocation-0")
            .resolve("_-screenshots")
        expectedDir.resolve("0-shot.png").shouldExist()
        outputDir.resolve("..").normalize().resolve("0-shot.png").shouldNotExist()
    }

    @Test
    fun `render escapes JSON-special characters in labels`(@TempDir outputDir: Path) {
        val driver = FakeBrowserDriver()
        val label = "q\"b\\n\nctrl"
        val screenshots = CapturedScreenshots().apply { capture(driver, label) }
        val attachments = Attachments().apply { put(SCREENSHOTS_KEY, screenshots) }
        val ctx = newCtx(outputDir, attachments = attachments)

        val json = ScreenshotsTabRenderer().render(ctx)
        requireNotNull(json)

        json shouldContain """"label":"q\"b\\n\nctrl""""
    }

    private fun newCtx(
        outputDir: Path,
        attachments: Attachments,
        tabId: String = "Screenshots"
    ): KensaTabContext = KensaTabContext(
        tabId = tabId,
        tabName = "Screenshots",
        invocationIdentifier = null,
        testClass = "FooTest",
        testMethod = "aTest",
        invocationIndex = 0,
        invocationDisplayName = "aTest",
        invocationState = "Passed",
        fixtures = Fixtures(),
        capturedOutputs = CapturedOutputs(),
        attachments = attachments,
        services = DefaultKensaTabServices(),
        outputDir = outputDir
    )
}
