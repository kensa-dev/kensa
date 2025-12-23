package dev.kensa.util

import dev.kensa.KensaException
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.misc.Interval
import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.ConcurrentHashMap

object SourceCode {
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

    fun existsFor(clazz: Class<*>): Boolean = loadCharStream(clazz) !== NullCharStream

    fun sourceStreamFor(clazz: Class<*>): CharStream = loadCharStream(clazz).takeUnless { it === NullCharStream } ?: throw KensaException("Could not locate source file for [${clazz.simpleName}]")

    private fun loadCharStream(clazz: Class<*>): CharStream = cache.getOrPut(clazz) {
        val name = clazz.name.substringBefore("$")
        val path = Walker(name, clazz.sourceExtension).run {
            Files.walkFileTree(workingDirectory, this)
            result
        }

        (path?.let { CharStreams.fromPath(it) } ?: NullCharStream).also { println("${clazz.simpleName} - ${it.sourceName}") }
    }
}

private class Walker(name: String, sourceExtension: String) : FileVisitor<Path?> {
    private val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/${name.replace("\\.".toRegex(), "/")}.$sourceExtension")
    var result: Path? = null

    override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes) = CONTINUE

    override fun visitFile(file: Path?, attrs: BasicFileAttributes) = CONTINUE.apply { if (pathMatcher.matches(file)) result = file }

    override fun visitFileFailed(file: Path?, exc: IOException?) = CONTINUE

    override fun postVisitDirectory(dir: Path?, exc: IOException?) = CONTINUE
}