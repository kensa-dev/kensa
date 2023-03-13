package dev.kensa.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration

internal class DurationFormatterTest {
    @Test
    internal fun formatsCorrectly() {
        DurationFormatter.format(Duration.ofMillis(1)) shouldBe "1 Ms"
        DurationFormatter.format(Duration.ofMillis(134)) shouldBe "134 Ms"
        DurationFormatter.format(Duration.ofSeconds(1)) shouldBe "1 Sec"
        DurationFormatter.format(Duration.ofSeconds(10)) shouldBe "10 Secs"
        DurationFormatter.format(Duration.ofSeconds(61)) shouldBe "1 Min 1 Sec"
        DurationFormatter.format(Duration.ofSeconds(122)) shouldBe "2 Mins 2 Secs"
        DurationFormatter.format(Duration.ofDays(1)) shouldBe "1 Day"
        DurationFormatter.format(Duration.ofMillis(894908787)) shouldBe "10 Days 8 Hrs 35 Mins 8 Secs 787 Ms"
    }
}