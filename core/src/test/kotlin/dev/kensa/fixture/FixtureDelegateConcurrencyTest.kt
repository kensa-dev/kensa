package dev.kensa.fixture

import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FixtureDelegateConcurrencyTest {

    private val referenceFx = fixture("ConcurrentReferenceFx") { "default-ref" }

    class UseCase(fx: Fixture<String>) {
        val reference: String by fixtures(fx)
    }

    @Test
    fun `one shared holder resolves each thread's own seeded value`() {
        val sharedUseCase = UseCase(referenceFx)
        val threads = 16
        val iterations = 200
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val observed = ConcurrentHashMap<String, MutableSet<String>>()

        repeat(threads) { t ->
            pool.submit {
                val expected = "REF-$t"
                val seen = observed.computeIfAbsent(expected) { ConcurrentHashMap.newKeySet() }
                start.await()
                repeat(iterations) {
                    val context = newContext().also { it.fixtures.seed(referenceFx, expected) }
                    TestContextHolder.bindToCurrentThread(context)
                    try {
                        seen.add(sharedUseCase.reference)
                    } finally {
                        TestContextHolder.clearFromThread()
                    }
                }
            }
        }

        start.countDown()
        pool.shutdown()
        pool.awaitTermination(30, TimeUnit.SECONDS) shouldBe true

        observed.forEach { (expected, seen) -> seen shouldBe setOf(expected) }
        observed.size shouldBe threads
    }

    private fun newContext() = TestContext(CapturedInteractions(SetupStrategy.Grouped), Fixtures(), CapturedOutputs())
}
