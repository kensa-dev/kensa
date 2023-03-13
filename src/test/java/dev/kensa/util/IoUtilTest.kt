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
import java.nio.file.Files
import java.nio.file.Path

internal class IoUtilTest {
    @Test
    internal fun canRecreateDirectory(@TempDir tempDir: Path) {
        createSomeFilesIn(tempDir.resolve("dir")).also { dir ->
            recreate(dir)
            Files.isDirectory(dir).shouldBeTrue()
            Files.list(dir).count() shouldBe 0
        }
    }

    @Test
    internal fun canCopyClasspathResource(@TempDir tempDir: Path) {
        val dir = Files.createDirectory(tempDir.resolve("dir"))
        copyResource("/dev/kensa/util/ioutil-copy-test.txt", dir)
        val copyContent = String(Files.readAllBytes(dir.resolve("ioutil-copy-test.txt")))
        copyContent shouldBe "Some file content..."
    }

    @Test
    internal fun copyClasspathResourceThrowsWhenDestinationNotADirectory(@TempDir tempDir: Path) {
        val destination = tempDir.resolve("foo.txt")
        shouldThrowExactly<KensaException> { copyResource("/dev/kensa/util/ioutil-copy-test.txt", destination) }
    }

    @Throws(IOException::class)
    private fun createSomeFilesIn(dir: Path): Path {
        val parent = Files.createDirectory(dir)
        (1..5).forEach { i ->
            Files.createFile(dir.resolve("file-$i.dat"))
        }
        return parent
    }
}