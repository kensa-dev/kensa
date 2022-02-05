package dev.kensa.util

import dev.kensa.KensaException
import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.attribute.BasicFileAttributes
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

object SourceCodeIndex {
    private val workingDirectory = Path.of(System.getProperty("user.dir"))

    private val KClass<*>.sourceExtension get() = if (isKotlinClass) "kt" else "java"

    fun locate(kClass: KClass<*>): Path =
        kClass.jvmName.substringBefore("$").let { name ->
            Walker(name, kClass.sourceExtension).run {
                Files.walkFileTree(workingDirectory, this)
                result ?: throw KensaException("Could not locate source file for [$name]")
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
}