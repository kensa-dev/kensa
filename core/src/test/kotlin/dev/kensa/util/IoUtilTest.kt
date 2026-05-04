package dev.kensa.util

import dev.kensa.KensaException
import dev.kensa.util.IoUtil.copyResource
import dev.kensa.util.IoUtil.recreate
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes

internal class IoUtilTest {
    @Test
    internal fun canRecreateDirectory(@TempDir tempDir: Path) {
        createSomeFilesIn(tempDir.resolve("dir")).also { dir ->
            recreate(dir)
            dir.isDirectory().shouldBeTrue()
            dir.listDirectoryEntries().size shouldBe 0
        }
    }

    @Test
    internal fun canCopyClasspathResource(@TempDir tempDir: Path) {
        val dir = tempDir.resolve("dir").createDirectory()
        copyResource("/dev/kensa/util/ioutil-copy-test.txt", dir)
        val copyContent = String(dir.resolve("ioutil-copy-test.txt").readBytes())
        copyContent shouldBe "Some file content..."
    }

    @Test
    internal fun copyClasspathResourceThrowsWhenDestinationNotADirectory(@TempDir tempDir: Path) {
        val destination = tempDir.resolve("foo.txt")
        shouldThrowExactly<KensaException> { copyResource("/dev/kensa/util/ioutil-copy-test.txt", destination) }
    }

    @Throws(IOException::class)
    private fun createSomeFilesIn(dir: Path): Path {
        val parent = dir.createDirectory()
        (1..5).forEach { i ->
            dir.resolve("file-$i.dat").createFile()
        }
        return parent
    }
}