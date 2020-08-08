package dev.kensa.util

import dev.kensa.KensaException
import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.attribute.BasicFileAttributes

object IoUtil {
    private val DELETE_ALL_VISITOR = DeleteAllVisitor()

    fun recreate(path: Path) {
        try {
            if (Files.exists(path)) {
                Files.walkFileTree(path, DELETE_ALL_VISITOR)
            }
            Files.createDirectories(path)
        } catch (e: IOException) {
            throw KensaException("Unable to recreate directory [$path]", e)
        }
    }

    fun copyResource(url: String, destDir: Path) {
        try {
            val resourceStream = IoUtil::class.java.getResourceAsStream(url) ?: throw KensaException("Did not find resource [$url]")
            if (!Files.isDirectory(destDir)) {
                throw KensaException("Destination [$destDir] is not a directory")
            }
            Files.copy(resourceStream, destDir.resolve(Paths.get(url).fileName))
        } catch (e: IOException) {
            throw KensaException("Unable to copy resource [$url] to [$destDir]", e)
        }
    }

    private class DeleteAllVisitor : SimpleFileVisitor<Path>() {
        override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
            file?.let {
                Files.delete(it)
            }
            return CONTINUE
        }

        override fun postVisitDirectory(directory: Path?, exc: IOException?): FileVisitResult {
            directory?.let {
                Files.delete(directory)
            }
            return CONTINUE
        }
    }
}