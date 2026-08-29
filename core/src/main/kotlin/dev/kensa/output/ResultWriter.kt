package dev.kensa.output

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.Section.Tabs
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.toIndexJson
import dev.kensa.output.json.JsonTransforms.toJsonString
import dev.kensa.output.json.JsonTransforms.toJsonWith
import dev.kensa.output.search.SearchIndexBuilder
import dev.kensa.output.search.SearchIndexWriter
import dev.kensa.render.diagram.ComponentDiagramFactory
import dev.kensa.sentence.Acronym
import dev.kensa.state.TestState.Disabled
import dev.kensa.state.TestState.Failed
import dev.kensa.state.TestState.NotExecuted
import dev.kensa.state.TestState.Passed
import dev.kensa.tabs.TabArtifactManager
import dev.kensa.util.IoUtil
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import com.eclipsesource.json.Json.`object` as jsonObject

internal class ResultWriter(private val configuration: Configuration, private val componentDiagramFactory: ComponentDiagramFactory) {

    private val startedAt: Instant = Instant.now()
    private val pid: Long = ProcessHandle.current().pid()
    private val hostname: String = hostname()

    // Running totals from every class written so far. Test threads finish
    // classes concurrently, so these are atomic and the marker snapshots them
    // under the write lock.
    private val classes = AtomicInteger()
    private val passed = AtomicInteger()
    private val failed = AtomicInteger()
    private val disabled = AtomicInteger()

    // One writer thread coalesces a burst of class completions into a single
    // marker write: a flush is only queued when none is already waiting, and
    // the queued flush snapshots whatever the counters hold when it runs.
    // Daemon, so a stuck flush can never hold the test JVM open.
    private val flusher = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "kensa-run-marker").apply { isDaemon = true }
    }
    private val flushPending = AtomicBoolean(false)

    @Volatile
    private var finishedAt: Instant? = null

    // Creating the writer is the start of a run: the previous bundle goes and
    // run.json marks the new one as in progress until write() finishes it.
    // Readers (the MCP server, the CLI) use the marker to tell a complete
    // bundle from one still being written or one whose JVM died.
    init {
        IoUtil.recreate(configuration.outputDir)
        writeRunMarker()
    }

    private val tabArtifactManager = TabArtifactManager()

    fun write(containers: List<TestContainer>) {
        val sortedContainers = containers.sortedBy { it.testClass.name }
        writeIndices(sortedContainers)
        writeSearchIndex(sortedContainers)
        writeConfiguration()
        if (!configuration.dataOnly) {
            writeHtml()
            IoUtil.copyResource("/kensa.js", configuration.outputDir)
            IoUtil.copyResource("/logo.svg", configuration.outputDir)
        }
        finish()

        println(
            """
                Kensa Output :
                ${configuration.outputDir.toAbsolutePath().resolve(if (configuration.dataOnly) "indices.json" else "index.html")}
            """.trimIndent()
        )
    }

    fun writeTest(container: TestContainer) {
        with(configuration) {
            val tabArtifacts = tabArtifactManager.generate(container, outputDir, configuration)

            val json = toJsonWith(renderers) { methodContainer, _, invocationIndex ->
                tabArtifacts[TabArtifactManager.InvocationKey(methodContainer.method.name, invocationIndex)] ?: emptyList()
            }(container)

            val string = toJsonString()(json)

            val resultsPath: Path = outputDir.resolve("results")
            resultsPath.createDirectories()
            resultsPath.resolve("${container.testClass.name}.json").writeText(string)
        }
        count(container)
        scheduleMarkerFlush()
    }

    private fun scheduleMarkerFlush() {
        if (finishedAt != null) return
        if (flushPending.compareAndSet(false, true)) {
            runCatching {
                flusher.execute {
                    flushPending.set(false)
                    writeRunMarker()
                }
            }
        }
    }

    // Drains any queued flush so the final write is the last one, then closes
    // the run. A class written after this point is counted but not flushed:
    // the marker must never go back to unfinished.
    private fun finish() {
        finishedAt = Instant.now()
        flusher.shutdown()
        flusher.awaitTermination(10, TimeUnit.SECONDS)
        writeRunMarker()
    }

    private fun count(container: TestContainer) {
        classes.incrementAndGet()
        container.methodContainers.values.forEach {
            when (it.state) {
                Passed -> passed.incrementAndGet()
                Failed -> failed.incrementAndGet()
                Disabled -> disabled.incrementAndGet()
                NotExecuted -> Unit
            }
        }
    }

    // The pid only means something on the host that ran the tests, so the
    // marker names the host and a reader elsewhere can decline to probe it.
    @Synchronized
    private fun writeRunMarker() {
        val json = jsonObject()
            .add("startedAt", startedAt.toString())
            .add("pid", pid)
            .add("hostname", hostname)
            .add("finishedAt", finishedAt?.toString())
            .add("classes", classes.get())
            .add("passed", passed.get())
            .add("failed", failed.get())
            .add("disabled", disabled.get())
        // Readers poll this file while it is being rewritten, so it is replaced
        // in one step rather than truncated and refilled in place.
        val marker = configuration.outputDir.resolve("run.json")
        val tmp = configuration.outputDir.resolve("run.json.tmp")
        tmp.writeText(json.toString())
        try {
            Files.move(tmp, marker, ATOMIC_MOVE, REPLACE_EXISTING)
        } catch (e: IOException) {
            // Windows refuses to replace a file a reader holds open, but allows
            // writing into it. A torn read there beats an exception on the
            // test thread.
            marker.writeText(json.toString())
            Files.deleteIfExists(tmp)
        }
    }

    // Not InetAddress.getLocalHost(): that resolves the name through DNS and
    // can stall for seconds on a machine whose name does not resolve, and this
    // runs on the first test's thread.
    private fun hostname(): String =
        System.getenv("HOSTNAME")?.takeIf { it.isNotBlank() }
            ?: System.getenv("COMPUTERNAME")?.takeIf { it.isNotBlank() }
            ?: runCatching {
                val process = ProcessBuilder("hostname").redirectErrorStream(true).start()
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroyForcibly()
                    ""
                } else process.inputStream.bufferedReader().readText().trim()
            }.getOrDefault("")

    private fun writeConfiguration() =
        with(configuration) {
            val kensaVersion = ResultWriter::class.java.`package`?.implementationVersion ?: "dev"
            val json = jsonObject()
                .add("autoOpenTab", autoOpenTab.name)
                .add("autoExpandNotes", autoExpandNotes)
                .add("titleText", titleText)
                .add("issueTrackerUrl", issueTrackerUrl?.toString())
                .add("acronyms", acronymsAsJson(dictionary.acronyms))
                .add("flattenPackages", flattenOutputPackages)
                .add("packageDisplay", packageDisplay.name)
                .add("packageDisplayRoot", packageDisplayRoot)
                .add("programme", programme)
                .add("service", service)
                .add("kensaVersion", kensaVersion)
                .add("generatedAt", Instant.now().toString())
                .add("sectionOrder", Json.array().apply {
                    sectionOrder.forEach {
                        add((if (it == Tabs) Tabs else it).name)
                    }
                })

            outputDir.resolve("configuration.json").writeText(json.toString())
        }

    private fun acronymsAsJson(acronyms: Collection<Acronym>) = jsonObject().apply {
        acronyms.forEach {
            add(it.acronym, it.meaning)
        }
    }

    private fun writeHtml() {
        val html = """
            <!doctype html>
            <html lang="en">
            <head>
                <title>${configuration.titleText}</title>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <link rel="icon" type="image/svg+xml" href="logo.svg" />
                <title>${configuration.titleText}</title>
            </head>
            <body>
                <div id="root"></div>
                <script src="kensa.js"></script>
            </body>
            </html>
        """.trimIndent()
        configuration.outputDir.resolve("index.html").writeText(html)
    }

    private fun writeSearchIndex(containers: List<TestContainer>) {
        val terms = SearchIndexBuilder(configuration.renderers).build(containers)
        SearchIndexWriter().write(configuration.outputDir, terms)
    }

    private fun writeIndices(containers: List<TestContainer>) {
        with(configuration) {
            val aggregateEntries = containers
                .flatMap { it.orderedMethodContainers }
                .flatMap { it.invocations }
                .flatMap { it.interactions }
                .toSet()
            val aggregateDiagram = componentDiagramFactory.create(aggregateEntries)

            val json = jsonObject()
                .add(
                    "indices",
                    Json.array().apply {
                        containers.forEach { add(toIndexJson(it.testClass.name)(it)) }
                    }
                )
                .add("aggregateComponentDiagram", aggregateDiagram?.toString())
            val string = toJsonString()(json)

            val indicesPath: Path = outputDir.resolve("indices.json")
            indicesPath.writeText(string)
        }
    }
}
