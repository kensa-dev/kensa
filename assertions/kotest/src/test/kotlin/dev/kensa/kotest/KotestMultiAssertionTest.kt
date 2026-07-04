package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.Matcher
import io.kotest.matchers.be
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlin.time.TimeSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class KotestMultiAssertionTest {

    private val testContext = TestContext(CapturedInteractions(SetupStrategy.Ignored), Fixtures(), CapturedOutputs())
    private val harness = Harness()

    @BeforeEach
    fun setUp() {
        TestContextHolder.bindToCurrentThread(testContext)
    }

    @AfterEach
    fun tearDown() {
        TestContextHolder.clearFromThread()
    }

    @Test
    fun `passes when all assertions pass immediately`() {
        with(harness) {
            thenEventually(2.seconds) {
                then(StateCollector { "a" }, be("a"))
                and(StateCollector { 42 }) { this shouldBe 42 }
            }
        }
    }

    @Test
    fun `empty block passes`() {
        with(harness) {
            thenEventually(2.seconds) {
            }
        }
    }

    @Test
    fun `assertion passing late succeeds within the shared window`() {
        val slowCount = AtomicInteger()

        with(harness) {
            thenEventually(5.seconds) {
                then(StateCollector { "a" }, be("a"))
                and(StateCollector { if (slowCount.incrementAndGet() < 4) "wrong" else "b" }, be("b"))
            }
        }

        slowCount.get() shouldBe 4
    }

    @Test
    fun `passed assertion locks in and stops polling even if it would later regress`() {
        val fastCount = AtomicInteger()
        val slowCount = AtomicInteger()

        with(harness) {
            thenEventually(5.seconds) {
                then(StateCollector { if (fastCount.incrementAndGet() == 1) "a" else "regressed" }, be("a"))
                and(StateCollector { if (slowCount.incrementAndGet() < 4) "wrong" else "b" }, be("b"))
            }
        }

        fastCount.get() shouldBe 1
    }

    @Test
    fun `assertions poll in parallel not sequentially`() {
        val active = AtomicInteger()
        val maxActive = AtomicInteger()
        fun blockingCollector() = StateCollector {
            maxActive.accumulateAndGet(active.incrementAndGet(), ::maxOf)
            Thread.sleep(100)
            active.decrementAndGet()
            if (maxActive.get() > 1) "pass" else "wrong"
        }

        with(harness) {
            thenEventually(10.seconds) {
                then(blockingCollector(), be("pass"))
                and(blockingCollector(), be("pass"))
                and(blockingCollector(), be("pass"))
            }
        }

        maxActive.get() shouldBeGreaterThan 1
    }

    @Test
    fun `single failing assertion rethrows its underlying error directly`() {
        val error = shouldThrow<AssertionError> {
            with(harness) {
                thenEventually(300.milliseconds) {
                    then(StateCollector { "a" }, be("a"))
                    and(StateCollector { "wrong" }, be("b"))
                }
            }
        }

        error shouldHaveMessage "expected:<b> but was:<wrong>"
    }

    @Test
    fun `multiple failing assertions throw one aggregate error listing only the failures`() {
        val error = shouldThrow<AssertionError> {
            with(harness) {
                thenEventually(300.milliseconds) {
                    then(StateCollector { "a" }, be("a"))
                    and(StateCollector { "wrong-b" }, be("b"))
                    and(StateCollector { 1 }, be(2))
                }
            }
        }

        error.message shouldContain "2 of 3 assertions did not pass within"
        error.message shouldContain "[2] expected:<b> but was:<wrong-b>"
        error.message shouldContain "[3] expected:<2> but was:<1>"
        error.message shouldNotContain "[1]"
        error.suppressedExceptions.shouldHaveSize(1)
    }

    @Test
    fun `spec onMatch fires exactly once at lock-in while a sibling is still polling`() {
        val onMatchCalls = AtomicInteger()
        val slowCount = AtomicInteger()
        val spec = object : ThenSpec<String> {
            override val collector = StateCollector { "a" }
            override val matcher: Matcher<String> = be("a")
            override val onMatch: CollectorContext.(String) -> Unit = { onMatchCalls.incrementAndGet() }
        }

        with(harness) {
            thenEventually(5.seconds) {
                then(spec)
                and(StateCollector { if (slowCount.incrementAndGet() < 4) "wrong" else "b" }, be("b"))
            }
        }

        onMatchCalls.get() shouldBe 1
    }

    @Test
    fun `onMatch failure is not retried and reports the original exception`() {
        val onMatchCalls = AtomicInteger()
        val spec = object : ThenSpec<String> {
            override val collector = StateCollector { "a" }
            override val matcher: Matcher<String> = be("a")
            override val onMatch: CollectorContext.(String) -> Unit = { onMatchCalls.incrementAndGet(); throw IllegalStateException("onMatch throws") }
        }

        shouldThrow<IllegalStateException> {
            with(harness) {
                thenEventually(2.seconds) {
                    then(spec)
                }
            }
        }.shouldHaveMessage("onMatch throws")

        onMatchCalls.get() shouldBe 1
    }

    @Test
    fun `matcher block and spec forms are mixable in one block`() {
        val spec = object : ThenSpec<String> {
            override val collector = StateCollector { "a" }
            override val matcher: Matcher<String> = be("a")
        }

        with(harness) {
            thenEventually(2.seconds) {
                then(spec)
                and(StateCollector { "b" }, be("b"))
                and(StateCollector { 42 }) { this shouldBe 42 }
            }
        }
    }

    @Test
    fun `continually block passes when all assertions hold for the whole duration`() {
        val aCount = AtomicInteger()
        val bCount = AtomicInteger()
        val start = TimeSource.Monotonic.markNow()

        with(harness) {
            thenContinually(300.milliseconds) {
                then(StateCollector { aCount.incrementAndGet(); "a" }, be("a"))
                and(StateCollector { bCount.incrementAndGet(); 1 }, be(1))
            }
        }

        start.elapsedNow() shouldBeGreaterThanOrEqualTo 300.milliseconds
        aCount.get() shouldBeGreaterThanOrEqual 2
        bCount.get() shouldBeGreaterThanOrEqual 2
    }

    @Test
    fun `continually block fails immediately when one assertion fails mid-window`() {
        val bCount = AtomicInteger()
        val start = TimeSource.Monotonic.markNow()

        val error = shouldThrow<AssertionError> {
            with(harness) {
                thenContinually(30.seconds) {
                    then(StateCollector { "a" }, be("a"))
                    and(StateCollector { if (bCount.incrementAndGet() < 3) 1 else 2 }, be(1))
                }
            }
        }

        start.elapsedNow() shouldBeLessThan 5.seconds
        error.message shouldContain "expected:<1> but was:<2>"
    }

    @Test
    fun `empty continually block passes`() {
        with(harness) {
            thenContinually(300.milliseconds) {
            }
        }
    }

    @Test
    fun `spec onMatch fires exactly once in continually block`() {
        val collectorCalls = AtomicInteger()
        val onMatchCalls = AtomicInteger()
        val spec = object : ThenSpec<String> {
            override val collector = StateCollector { collectorCalls.incrementAndGet(); "a" }
            override val matcher: Matcher<String> = be("a")
            override val onMatch: CollectorContext.(String) -> Unit = { onMatchCalls.incrementAndGet() }
        }

        with(harness) {
            thenContinually(300.milliseconds) {
                then(spec)
            }
        }

        collectorCalls.get() shouldBeGreaterThanOrEqual 2
        onMatchCalls.get() shouldBe 1
    }

    @Test
    fun `onMatch failure in continually block reports original exception`() {
        val spec = object : ThenSpec<String> {
            override val collector = StateCollector { "a" }
            override val matcher: Matcher<String> = be("a")
            override val onMatch: CollectorContext.(String) -> Unit = { throw IllegalStateException("onMatch throws") }
        }

        shouldThrow<IllegalStateException> {
            with(harness) {
                thenContinually(2.seconds) {
                    then(spec)
                }
            }
        }.shouldHaveMessage("onMatch throws")
    }

    class Harness : WithKotest
}
