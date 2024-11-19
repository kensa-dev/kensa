package dev.kensa.util

import dev.kensa.KensaException
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.attribute.BasicFileAttributes

object SourceCode {
    private val workingDirectory = Path.of(System.getProperty("user.dir"))

    private val Class<*>.sourceExtension get() = if (isKotlinClass) "kt" else "java"

    fun sourceStreamFor(clazz: Class<*>): CharStream =
        CharStreams.fromPath(clazz.name.substringBefore("$").let { name ->
            Walker(name, clazz.sourceExtension).run {
                Files.walkFileTree(workingDirectory, this)
                result ?: throw KensaException("Could not locate source file for [$name]")
            }
        }
        )

    private class Walker(name: String, sourceExtension: String) : FileVisitor<Path?> {
        private val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/${name.replace("\\.".toRegex(), "/")}.$sourceExtension")
        var result: Path? = null

        override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes) = CONTINUE

        override fun visitFile(file: Path?, attrs: BasicFileAttributes) = CONTINUE.apply { if (pathMatcher.matches(file)) result = file }

        override fun visitFileFailed(file: Path?, exc: IOException?) = CONTINUE

        override fun postVisitDirectory(dir: Path?, exc: IOException?) = CONTINUE
    }
}