package dev.kensa.util

import dev.kensa.KensaException
import dev.kensa.util.IoUtil.copyResource
import dev.kensa.util.IoUtil.recreate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class IoUtilTest {
    @Test
    internal fun canRecreateDirectory(@TempDir tempDir: Path) {
        val dir = createSomeFilesIn(tempDir.resolve("dir"))
        recreate(dir)
        assertThat(Files.isDirectory(dir)).isTrue()
        assertThat(Files.list(dir).count()).isEqualTo(0)
    }

    @Test
    internal fun canCopyClasspathResource(@TempDir tempDir: Path) {
        val dir = Files.createDirectory(tempDir.resolve("dir"))
        copyResource("/dev/kensa/util/ioutil-copy-test.txt", dir)
        val copyContent = String(Files.readAllBytes(dir.resolve("ioutil-copy-test.txt")))
        assertThat(copyContent).isEqualTo("Some file content...")
    }

    @Test
    internal fun copyClasspathResourceThrowsWhenDestinationNotADirectory(@TempDir tempDir: Path) {
        val destination = tempDir.resolve("foo.txt")
        org.junit.jupiter.api.Assertions.assertThrows(
                KensaException::class.java
        ) { copyResource("/dev/kensa/util/ioutil-copy-test.txt", destination) }
    }

    @Throws(IOException::class)
    private fun createSomeFilesIn(dir: Path): Path {
        val parent = Files.createDirectory(dir)
        for (i in 1..5) {
            Files.createFile(dir.resolve("file-$i.dat"))
        }
        return parent
    }
}