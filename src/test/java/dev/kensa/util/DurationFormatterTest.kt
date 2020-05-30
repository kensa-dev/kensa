package dev.kensa.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

internal class DurationFormatterTest {
    @Test
    internal fun formatsCorrectly() {
        assertThat(DurationFormatter.format(Duration.ofMillis(1))).isEqualTo("1 Ms")
        assertThat(DurationFormatter.format(Duration.ofMillis(134))).isEqualTo("134 Ms")
        assertThat(DurationFormatter.format(Duration.ofSeconds(1))).isEqualTo("1 Sec")
        assertThat(DurationFormatter.format(Duration.ofSeconds(10))).isEqualTo("10 Secs")
        assertThat(DurationFormatter.format(Duration.ofSeconds(61))).isEqualTo("1 Min 1 Sec")
        assertThat(DurationFormatter.format(Duration.ofSeconds(122))).isEqualTo("2 Mins 2 Secs")
        assertThat(DurationFormatter.format(Duration.ofDays(1))).isEqualTo("1 Day")
        assertThat(DurationFormatter.format(Duration.ofMillis(894908787))).isEqualTo("10 Days 8 Hrs 35 Mins 8 Secs 787 Ms")
    }
}