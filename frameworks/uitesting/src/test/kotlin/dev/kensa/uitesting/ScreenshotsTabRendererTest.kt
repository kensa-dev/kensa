package dev.kensa.uitesting

import dev.kensa.attachments.Attachments
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.tabs.DefaultKensaTabServices
import dev.kensa.tabs.KensaTabContext
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
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
    fun `render writes PNGs to disk and references them by relative URL`(@TempDir outputDir: Path) {
        val driver = FakeBrowserDriver()
        val screenshots = CapturedScreenshots()
        screenshots.capture(driver, "before")
        screenshots.capture(driver, "after")

        val attachments = Attachments().apply { put(SCREENSHOTS_KEY, screenshots) }
        val ctx = newCtx(outputDir, attachments = attachments, tabId = "Screenshots")

        val html = ScreenshotsTabRenderer().render(ctx)
        requireNotNull(html)

        html shouldContain """src="tabs/FooTest/aTest/invocation-0/Screenshots-screenshots/0-before.png""""
        html shouldContain """src="tabs/FooTest/aTest/invocation-0/Screenshots-screenshots/1-after.png""""
        html shouldNotContain "data:image/png;base64"

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
