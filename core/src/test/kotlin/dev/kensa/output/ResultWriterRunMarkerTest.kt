package dev.kensa.output

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import dev.kensa.Configuration
import dev.kensa.context.TestContainer
import dev.kensa.output.json.fakeTestContainer
import dev.kensa.output.json.fakeTestMethodContainer
import dev.kensa.render.diagram.ComponentDiagramFactory
import dev.kensa.state.TestState
import dev.kensa.state.TestState.Disabled
import dev.kensa.state.TestState.Failed
import dev.kensa.state.TestState.Passed
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.io.path.readText

class ResultWriterRunMarkerTest {

    @Test
    fun `creating the writer marks the run as started`(@TempDir tempDir: Path) {
        val before = Instant.now()
        ResultWriter(configuration(tempDir), ComponentDiagramFactory())

        val marker = marker(tempDir)
        Instant.parse(marker.getString("startedAt", null)).toEpochMilli() shouldBeGreaterThanOrEqual before.toEpochMilli()
        marker.getLong("pid", -1) shouldBe ProcessHandle.current().pid()
        marker.getString("hostname", "").shouldNotBeBlank()
        marker.get("finishedAt").isNull shouldBe true
        tempDir.resolve("indices.json").shouldNotExist()
    }

    @Test
    fun `writing the results marks the run as finished after the indices are on disk`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())
        val startedAt = marker(tempDir).getString("startedAt", null)

        writer.write(emptyList())

        val marker = marker(tempDir)
        marker.getString("startedAt", null) shouldBe startedAt
        marker.getLong("pid", -1) shouldBe ProcessHandle.current().pid()
        Instant.parse(marker.getString("finishedAt", null)).toEpochMilli() shouldBeGreaterThanOrEqual Instant.parse(startedAt).toEpochMilli()
        tempDir.resolve("indices.json").shouldExist()
    }

    @Test
    fun `writing a class adds its method states to the marker counts`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())

        writer.writeTest(container(Passed, Passed, Failed, Disabled))

        val marker = markerOnce(tempDir) { it.getInt("classes", -1) == 1 }
        marker.getInt("classes", -1) shouldBe 1
        marker.getInt("passed", -1) shouldBe 2
        marker.getInt("failed", -1) shouldBe 1
        marker.getInt("disabled", -1) shouldBe 1
        marker.get("finishedAt").isNull shouldBe true
    }

    @Test
    fun `counts accumulate across classes and survive finishing the run`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())

        writer.writeTest(container(Passed, Failed))
        writer.writeTest(container(Passed, Disabled))
        writer.write(emptyList())

        val marker = marker(tempDir)
        marker.getInt("classes", -1) shouldBe 2
        marker.getInt("passed", -1) shouldBe 2
        marker.getInt("failed", -1) shouldBe 1
        marker.getInt("disabled", -1) shouldBe 1
        marker.get("finishedAt").isNull shouldBe false
    }

    @Test
    fun `a reader never sees a partially written marker`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())
        val container = container(Passed, Failed)
        val writing = AtomicBoolean(true)
        val badReads = AtomicInteger()
        val reader = thread {
            while (writing.get()) {
                runCatching { marker(tempDir).getInt("classes", -1) }
                    .onFailure { badReads.incrementAndGet() }
                    .onSuccess { if (it < 0) badReads.incrementAndGet() }
            }
        }

        repeat(300) { writer.writeTest(container) }
        writer.write(emptyList())
        writing.set(false)
        reader.join()

        badReads.get() shouldBe 0
        tempDir.resolve("run.json.tmp").shouldNotExist()
    }

    @Test
    fun `classes written concurrently are all counted`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())
        val container = container(Passed, Failed)

        (1..32).map { thread { writer.writeTest(container) } }.forEach { it.join() }

        val marker = markerOnce(tempDir) { it.getInt("classes", -1) == 32 }
        marker.getInt("classes", -1) shouldBe 32
        marker.getInt("passed", -1) shouldBe 32
        marker.getInt("failed", -1) shouldBe 32
        marker.getInt("disabled", -1) shouldBe 0
    }

    @Test
    fun `a burst of classes is fully reflected once the run is finished`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())
        val container = container(Passed, Failed)

        (1..32).map { thread { writer.writeTest(container) } }.forEach { it.join() }
        writer.write(emptyList())

        val marker = marker(tempDir)
        marker.getInt("classes", -1) shouldBe 32
        marker.getInt("passed", -1) shouldBe 32
        marker.getInt("failed", -1) shouldBe 32
        marker.get("finishedAt").isNull shouldBe false
    }

    @Test
    fun `a class written after the run has finished does not reopen the marker`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())
        writer.write(emptyList())
        val finishedAt = marker(tempDir).getString("finishedAt", null)

        writer.writeTest(container(Passed))

        marker(tempDir).getString("finishedAt", null) shouldBe finishedAt
    }

    private fun marker(dir: Path): JsonObject = Json.parse(dir.resolve("run.json").readText()).asObject()

    private fun markerOnce(dir: Path, condition: (JsonObject) -> Boolean): JsonObject {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (true) {
            val marker = marker(dir)
            if (condition(marker) || System.nanoTime() > deadline) return marker
            Thread.sleep(10)
        }
    }

    private fun container(vararg states: TestState): TestContainer {
        val methods = Sample::class.java.declaredMethods.sortedBy { it.name }
        val methodContainers = states.mapIndexed { i, state -> fakeTestMethodContainer(method = methods[i], state = state) }
        return fakeTestContainer(testClass = Sample::class.java, methodContainers = methodContainers)
    }

    @Suppress("unused")
    private class Sample {
        fun a() {}
        fun b() {}
        fun c() {}
        fun d() {}
    }

    private fun configuration(dir: Path) = Configuration().apply {
        outputDir = dir
        dataOnly = true
    }
}
