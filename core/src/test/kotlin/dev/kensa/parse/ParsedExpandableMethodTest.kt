package dev.kensa.parse

import dev.kensa.sentence.TemplateSentence
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class ParsedExpandableMethodTest {

    private val params = MethodParameters(emptyMap())

    @Test
    fun `provider is not invoked at construction`() {
        val calls = AtomicInteger(0)
        ParsedExpandableMethod("greet", params) {
            calls.incrementAndGet()
            emptyList()
        }
        calls.get() shouldBe 0
    }

    @Test
    fun `provider is invoked on first sentences access`() {
        val calls = AtomicInteger(0)
        val pem = ParsedExpandableMethod("greet", params) {
            calls.incrementAndGet()
            emptyList()
        }
        pem.sentences
        calls.get() shouldBe 1
    }

    @Test
    fun `provider is invoked at most once across repeated accesses`() {
        val calls = AtomicInteger(0)
        val sentences = listOf(TemplateSentence(emptyList()))
        val pem = ParsedExpandableMethod("greet", params) {
            calls.incrementAndGet()
            sentences
        }
        repeat(5) { pem.sentences }
        calls.get() shouldBe 1
        pem.sentences shouldBe sentences
    }

    @Test
    fun `eager constructor returns the supplied list and never re-evaluates`() {
        val sentences = listOf(TemplateSentence(emptyList()))
        val pem = ParsedExpandableMethod("greet", params, sentences)
        pem.sentences shouldBe sentences
        pem.sentences shouldBe sentences
    }

    @Test
    fun `concurrent first access invokes the provider exactly once`() {
        val threads = 16
        val calls = AtomicInteger(0)
        val ready = CountDownLatch(threads)
        val go = CountDownLatch(1)
        val pem = ParsedExpandableMethod("greet", params) {
            calls.incrementAndGet()
            emptyList()
        }
        val pool = Executors.newFixedThreadPool(threads)
        try {
            repeat(threads) {
                pool.submit {
                    ready.countDown()
                    go.await()
                    pem.sentences
                }
            }
            ready.await()
            go.countDown()
            pool.shutdown()
            pool.awaitTermination(5, TimeUnit.SECONDS) shouldBe true
        } finally {
            pool.shutdownNow()
        }
        calls.get() shouldBe 1
    }
}
