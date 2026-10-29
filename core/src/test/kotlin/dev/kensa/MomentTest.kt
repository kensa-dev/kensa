package dev.kensa

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.time.Duration as JavaDuration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class MomentTest {

    @Test
    fun `marking an action records the moment after the action completes`() {
        val moment = Moment()
        val action = Action<String> { shouldThrow<IllegalStateException> { 1.seconds since moment } }.marking(moment)

        action.execute("ignored")

        (10.seconds since moment) shouldBeLessThanOrEqualTo 10.seconds
    }

    @Test
    fun `the window remaining is the window less the time since the moment`() {
        val moment = Moment().apply { mark() }
        Thread.sleep(50)

        val remaining = 1.seconds since moment

        remaining shouldBeLessThanOrEqualTo 950.milliseconds
        remaining shouldBeGreaterThan 500.milliseconds
    }

    @Test
    fun `a window that has already passed fails rather than passing trivially`() {
        val moment = Moment().apply { mark() }
        Thread.sleep(20)

        val error = shouldThrow<AssertionError> { 10.milliseconds since moment }

        error.message shouldContain "10ms"
        error.message shouldContain "already passed"
    }

    @Test
    fun `an unmarked moment is an error`() {
        shouldThrow<IllegalStateException> { 1.seconds since Moment() }.message shouldContain "not been marked"
    }

    @Test
    fun `java callers measure the remaining window as a java Duration`() {
        val moment = Moment().apply { mark() }
        Thread.sleep(50)

        moment.remaining(JavaDuration.ofSeconds(5)) shouldBeLessThanOrEqualTo JavaDuration.ofMillis(4950)
    }
}
