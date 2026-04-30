package dev.kensa.uitesting

import dev.kensa.Configuration
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

class UiTestingConfigTest {

    @Test
    fun `uiTesting returns a default UiTestingConfig with autoScreenshotOnFailure off`() {
        val configuration = Configuration()

        configuration.uiTesting.autoScreenshotOnFailure shouldBe false
    }

    @Test
    fun `mutating uiTesting persists across reads on the same Configuration`() {
        val configuration = Configuration()

        configuration.uiTesting.autoScreenshotOnFailure = true

        configuration.uiTesting.autoScreenshotOnFailure shouldBe true
    }

    @Test
    fun `repeated reads return the same UiTestingConfig instance`() {
        val configuration = Configuration()

        val first = configuration.uiTesting
        val second = configuration.uiTesting

        first shouldBeSameInstanceAs second
    }

    @Test
    fun `uiTesting on different Configuration instances is independent`() {
        val a = Configuration()
        val b = Configuration()

        a.uiTesting.autoScreenshotOnFailure = true

        b.uiTesting.autoScreenshotOnFailure shouldBe false
    }
}
