package dev.kensa.util

import dev.kensa.KensaException
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.misc.Interval
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.toPath

class SourceCode(private val sourceLocations : () -> List<Path> = { emptyList() }) {
    private val workingDirectory = Path.of(System.getProperty("user.dir"))
    private val Class<*>.sourceExtension get() = if (isKotlinClass) "kt" else "java"

    private object NullCharStream : CharStream {
        override fun getText(interval: Interval): String = ""
        override fun consume() {}
        override fun LA(i: Int): Int = 0
        override fun mark(): Int = 0
        override fun release(marker: Int) {}
        override fun index(): Int = 0
        override fun seek(index: Int) {}
        override fun size(): Int = 0
        override fun getSourceName(): String = "NotFound"
    }

    private val cache = ConcurrentHashMap<Class<*>, CharStream>()
    private val fsCache = ConcurrentHashMap<URI, FileSystem>()
    private val defaultSourceLocations: List<Path> =
        listOf(
            workingDirectory.resolve("src/main/java"),
            workingDirectory.resolve("src/main/kotlin"),
            workingDirectory.resolve("src/test/java"),
            workingDirectory.resolve("src/test/kotlin")
        )

    private val discoveredLocations = ConcurrentHashMap.newKeySet<Path>()

    fun existsFor(clazz: Class<*>): Boolean = loadCharStream(clazz) !== NullCharStream

    fun sourceStreamFor(clazz: Class<*>): CharStream = loadCharStream(clazz).takeUnless { it === NullCharStream } ?: throw KensaException("Could not locate source file for [${clazz.simpleName}]")

    private fun loadCharStream(clazz: Class<*>): CharStream = cache.getOrPut(clazz) {
        val name = clazz.name.substringBefore("$")
        val internalPath = "${name.replace(".", "/")}.${clazz.sourceExtension}"

        internalPath.findInSourceLocations()
            ?: internalPath.findInDiscoveredSources(clazz)
            ?: NullCharStream
    }

    private fun String.findInSourceLocations(): CharStream? =
        allLocations()
            .map { it.toAbsolutePath().toUri().asRootPath().resolve(this) }
            .find { it.exists() }
            ?.let { CharStreams.fromPath(it) }

    private fun allLocations() = sequenceOf(
        defaultSourceLocations.asSequence(),
        sourceLocations().asSequence(),
        discoveredLocations.asSequence()
    ).flatten()

    private fun String.findInDiscoveredSources(clazz: Class<*>): CharStream? =
        if (clazz.tryRegisterSourcesJar()) findInSourceLocations() else null

    private fun Class<*>.tryRegisterSourcesJar(): Boolean {
        try {
            val location = protectionDomain?.codeSource?.location?.toURI() ?: return false
            val path = Paths.get(location)

            if (path.endsWith(".jar")) {
                val sourceJarPaths = listOf(
                    path.toString().replace(".jar", "-sources.jar"),
                    path.toString().replace(".jar", "-src.jar")
                ).map { Path(it) }

                sourceJarPaths.find { it.exists() }?.let {
                    discoveredLocations.add(it)
                    return true
                }
            }
        } catch (e: Exception) {
            System.err.println("Failed to find source jar for [${this.name}]")
            e.printStackTrace(System.err)
        }
        return false
    }

    private fun URI.asRootPath(): Path =
        if (path?.endsWith(".jar") == true) {
            val jarUri = URI.create("jar:${this}")
            val fs = fsCache.getOrPut(jarUri) {
                FileSystems.newFileSystem(jarUri, emptyMap<String, Any>())
            }
            fs.getPath("/")
        } else {
            toPath()
        }
}