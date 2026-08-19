package dev.kensa.state

import dev.kensa.Tab
import dev.kensa.context.TestContext
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.output.json.fakeTestInvocation
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Concurrent invocations of a single test method (a parameterised test under
 * `@Execution(CONCURRENT)`) all share one [TestMethodContainer].
 */
@Execution(ExecutionMode.SAME_THREAD)
class TestMethodContainerConcurrencyTest {

    @Suppress("UNUSED")
    fun sampleTestMethod() = Unit

    @Test
    fun `records every concurrent invocation of the same test method`() {
        val method = this::class.java.getDeclaredMethod("sampleTestMethod")
        val factory = mock<TestInvocationFactory>()
        whenever(factory.create(any(), any(), any(), anyOrNull(), any()))
            .thenAnswer { fakeTestInvocation() to emptyList<dev.kensa.parse.ParseError>() }

        val container = TestMethodContainer(factory, method, "sample", emptyList(), emptyList(), null, TestState.NotExecuted, Tab.None)

        val invocations = 500
        val threads = 16
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val failures = CopyOnWriteArrayList<Throwable>()

        repeat(invocations) {
            pool.submit {
                try {
                    start.await()
                    val context = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
                    val id = container.startTestInvocation(this, emptyList(), "sample", 0L, context)
                    container.endTestInvocation(context, id, null, 1L)
                } catch (t: Throwable) {
                    failures.add(t)
                }
            }
        }

        start.countDown()
        pool.shutdown()
        pool.awaitTermination(60, TimeUnit.SECONDS) shouldBe true

        failures.map { "${it::class.simpleName}: ${it.message}" } shouldBe emptyList()
        container.invocations.size shouldBe invocations
        container.invocationContexts.size shouldBe invocations
    }

}
