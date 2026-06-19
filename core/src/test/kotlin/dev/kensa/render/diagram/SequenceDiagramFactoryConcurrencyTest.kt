package dev.kensa.render.diagram

import dev.kensa.Configuration
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Party
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

internal class SequenceDiagramFactoryConcurrencyTest {

    private fun party(name: String): Party = object : Party { override fun asString() = name }

    @Test
    fun `create does not fail when directives are structurally mutated concurrently`() {
        val configuration = Configuration()
        val factory = SequenceDiagramFactory({ configuration.sequenceDiagram.directivesSnapshot() })

        val interactions = CapturedInteractions(SetupStrategy.Ungrouped)
        interactions.capture(from(party("A")).to(party("B")).with("payload", "sends a request"))

        val errors = CopyOnWriteArrayList<Throwable>()
        val stop = AtomicBoolean(false)
        val ready = CountDownLatch(2)

        val writer = Thread {
            ready.countDown(); ready.await()
            var i = 0
            while (!stop.get()) {
                configuration.sequenceDiagram.participant("P${i++}")
                if (i % 16 == 0) configuration.sequenceDiagram.reset()
            }
        }
        val reader = Thread {
            ready.countDown(); ready.await()
            repeat(5_000) {
                try {
                    factory.create(interactions)
                } catch (t: Throwable) {
                    errors += t
                }
            }
            stop.set(true)
        }

        writer.start(); reader.start()
        reader.join(); writer.join()

        errors shouldBe emptyList()
    }
}
