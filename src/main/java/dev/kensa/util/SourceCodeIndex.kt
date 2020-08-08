package dev.kensa.util

import dev.kensa.KensaException
import dev.kensa.Language
import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.attribute.BasicFileAttributes
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

class SourceCodeIndex private constructor() {
    private val workingDirectory = Paths.get(System.getProperty("user.dir"))

    private fun doLocate(kClass: KClass<*>): Path {
        val language = Language.of(kClass)
        var name = kClass.jvmName
        val s = name.indexOf("$")
        if (s > 0) {
            name = name.substring(0, s)
        }
        Walker(name, language).let { walker ->
            try {
                Files.walkFileTree(workingDirectory, walker)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return walker.result()
        }
    }

    private class Walker internal constructor(private val name: String, language: Language) : FileVisitor<Path?> {
        private val pathMatcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/${name.replace("\\.".toRegex(), "/")}.${language.sourceFileExtension}")
        private var result: Path? = null

        override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes): FileVisitResult = CONTINUE

        override fun visitFile(file: Path?, attrs: BasicFileAttributes): FileVisitResult {
            if (pathMatcher.matches(file)) {
                result = file
            }
            return CONTINUE
        }

        override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult = CONTINUE

        override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult = CONTINUE

        fun result(): Path = result ?: throw KensaException("Could not locate source file for [$name]")
    }

    companion object {
        private val INSTANCE = SourceCodeIndex()

        fun locate(kClass: KClass<*>): Path = INSTANCE.doLocate(kClass)
    }
}