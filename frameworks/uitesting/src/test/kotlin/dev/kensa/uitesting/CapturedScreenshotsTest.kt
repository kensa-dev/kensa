package dev.kensa.uitesting

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class CapturedScreenshotsTest {

    @AfterEach fun tearDown() = CapturedScreenshots.clear()

    @Test
    fun `current throws when no instance is bound`() {
        val ex = shouldThrow<IllegalStateException> { CapturedScreenshots.current() }
        ex.message shouldContain "KensaUiExtension"
    }

    @Test
    fun `bind replaces the thread-local instance`() {
        val a = CapturedScreenshots()
        val b = CapturedScreenshots()
        CapturedScreenshots.bind(a)
        CapturedScreenshots.current() shouldBeSameInstanceAs a
        CapturedScreenshots.bind(b)
        CapturedScreenshots.current() shouldBeSameInstanceAs b
    }

    @Test
    fun `capture appends a screenshot using the driver bytes`() {
        val driver = FakeBrowserDriver()
        val screenshots = CapturedScreenshots()
        screenshots.capture(driver, "before")
        screenshots.capture(driver, "after")
        screenshots.all().shouldHaveSize(2)
        screenshots.all()[0].label shouldBe "before"
        screenshots.all()[0].format shouldBe "png"
    }

    @Test
    fun `clear removes the thread-local instance`() {
        CapturedScreenshots.bind(CapturedScreenshots())
        CapturedScreenshots.clear()
        shouldThrow<IllegalStateException> { CapturedScreenshots.current() }
    }
}
