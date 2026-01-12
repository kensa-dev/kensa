package dev.kensa

import dev.kensa.example.KotlinInterface
import dev.kensa.util.SourceCode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.misc.Interval
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.readText

internal class SourceCodeTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `can locate source in the local project`() {
        val stream = SourceCode().sourceStreamFor(SourceCodeTest::class.java)

        stream.sourceName shouldEndWith "src/test/kotlin/dev/kensa/SourceCodeTest.kt"
        stream.getText(Interval.of(0, 18)) shouldBe "package dev.kensa\n\n"
    }

    @Test
    fun `can locate source in a registered sibling project directory`() {
        val siblingRoot = Path("src/example/kotlin")
        val sourceFile = siblingRoot.resolve("dev/kensa/example/KotlinInterface.kt")

        val stream = SourceCode { listOf(siblingRoot) }.sourceStreamFor(KotlinInterface::class.java)

        stream.sourceName shouldBe sourceFile.toAbsolutePath().toString()
        stream.getText(Interval.of(78, 102)) shouldBe "interface KotlinInterface"
    }

    @Test
    fun `can locate source inside a registered JAR file`() {
        val sourceLocation = Path("src/example/kotlin/dev/kensa/example/KotlinInterface.kt")
        val jarPath = tempDir.resolve("external-sources.jar")
        createJarWithSource(jarPath, "dev/kensa/example/KotlinInterface.kt", sourceLocation.readText())

        val stream = SourceCode({ listOf(jarPath) }).sourceStreamFor(KotlinInterface::class.java)

        stream.sourceName shouldBe "/dev/kensa/example/KotlinInterface.kt"
        stream.getText(Interval.of(78, 102)) shouldBe "interface KotlinInterface"
    }

    private fun createJarWithSource(path: Path, internalPath: String, content: String) {
        ZipOutputStream(FileOutputStream(path.toFile())).use { zos ->
            zos.putNextEntry(ZipEntry(internalPath))
            zos.write(content.toByteArray())
            zos.closeEntry()
        }
    }

    @Test
    fun canLocateByInnerClass() {
        val stream: CharStream = SourceCode().sourceStreamFor(InnerClass::class.java)

        stream.sourceName shouldEndWith "src/test/kotlin/dev/kensa/SourceCodeTest.kt"
        stream.getText(Interval.of(0, 18)) shouldBe "package dev.kensa\n\n"
    }

    internal class InnerClass
}