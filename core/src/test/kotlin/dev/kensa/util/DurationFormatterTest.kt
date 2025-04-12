package dev.kensa.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class DurationFormatterTest {
    @Test
    internal fun formatsCorrectly() {
        1.milliseconds.format() shouldBe "1 Ms"
        134.milliseconds.format() shouldBe "134 Ms"
        1.seconds.format() shouldBe "1 Sec"
        10.seconds.format() shouldBe "10 Secs"
        61.seconds.format() shouldBe "1 Min 1 Sec"
        122.seconds.format() shouldBe "2 Mins 2 Secs"
        1.days.format() shouldBe "1 Day"
        894908787.milliseconds.format() shouldBe "10 Days 8 Hrs 35 Mins 8 Secs 787 Ms"
    }
}