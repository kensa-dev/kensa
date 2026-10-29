package dev.kensa

import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

/**
 * A point in time recorded during a test, typically when an action completes, so that a later
 * `thenContinually` window can be measured from it rather than from the assertion call.
 *
 * ```kotlin
 * private val theTriggerWasSent = Moment()
 *
 * whenever(upstream.sends(aTrigger()).marking(theTriggerWasSent))
 *
 * thenContinually(5.seconds since theTriggerWasSent) {
 *     then(theDownstreamMessages(), beEmpty())
 * }
 * ```
 *
 * Mark it with [Action.marking] or directly with [mark]. A `Moment` keeps its mark, so it belongs to
 * one test: in a single-instance Kotest spec or a `PER_CLASS` JUnit class, declare it inside the test.
 */
class Moment {
    private val mark = AtomicReference<TimeMark?>()

    /** Records now as the moment. A later call replaces an earlier one. */
    fun mark() {
        mark.set(TimeSource.Monotonic.markNow())
    }

    /**
     * The part of [window] still to run, measured from this moment, for Java callers. See [since].
     */
    fun remaining(window: java.time.Duration): java.time.Duration = remaining(window.toKotlinDuration()).toJavaDuration()

    internal fun remaining(window: Duration): Duration {
        val elapsed = checkNotNull(mark.get()) { "The moment has not been marked. Mark it with Action.marking(moment) or moment.mark()." }.elapsedNow()
        val remaining = window - elapsed
        if (remaining <= Duration.ZERO) {
            throw AssertionError("The $window window since the moment had already passed when the assertion began: ${elapsed.inWholeMilliseconds}ms had elapsed. Lengthen the window or move the assertion earlier.")
        }
        return remaining
    }
}

/**
 * The part of this window still to run, measured from [moment].
 *
 * @throws IllegalStateException if the moment has not been marked.
 * @throws AssertionError if the window has already passed.
 */
infix fun Duration.since(moment: Moment): Duration = moment.remaining(this)
