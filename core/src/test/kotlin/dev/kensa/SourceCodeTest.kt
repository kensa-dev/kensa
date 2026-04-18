package dev.kensa

import dev.kensa.example.KotlinInterface
import dev.kensa.util.SourceCode
import dev.kensa.util.SourceCode.Companion.candidateSourcesJarNames
import dev.kensa.util.SourceCode.Companion.findSourcesJar
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.misc.Interval
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileOutputStream
import java.nio.file.Files
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

    @Test
    fun canLocateByInnerClass() {
        val stream: CharStream = SourceCode().sourceStreamFor(InnerClass::class.java)

        stream.sourceName shouldEndWith "src/test/kotlin/dev/kensa/SourceCodeTest.kt"
        stream.getText(Interval.of(0, 18)) shouldBe "package dev.kensa\n\n"
    }

    @Test
    fun `can locate source in a Gradle cached JAR using sibling hash directory`() {
        // Gradle files-2.1 layout:
        //   <group>/<artifact>/<version>/<hashA>/artifact.jar
        //   <group>/<artifact>/<version>/<hashB>/artifact-sources.jar
        val versionDir = tempDir.resolve("com.example/foo/1.0").also { Files.createDirectories(it) }
        val binaryJarDir = versionDir.resolve("abc123").also { Files.createDirectories(it) }
        val sourcesJarDir = versionDir.resolve("def456").also { Files.createDirectories(it) }

        val binaryJar = binaryJarDir.resolve("foo-1.0.jar")
        createJarWithClass(binaryJar)

        val sourcesJar = sourcesJarDir.resolve("foo-1.0-sources.jar")
        createJarWithSource(sourcesJar, "dev/kensa/example/KotlinInterface.kt", kotlinInterfaceSource())

        val sourceCode = sourceCodeWithJarLocation(binaryJar)
        val stream = sourceCode.sourceStreamFor(KotlinInterface::class.java)

        stream.sourceName shouldBe "/dev/kensa/example/KotlinInterface.kt"
        stream.getText(Interval.of(78, 102)) shouldBe "interface KotlinInterface"
    }

    @Test
    fun `can locate source in a Maven cached JAR using same directory`() {
        // Maven .m2 layout:
        //   <group-path>/<artifact>/<version>/artifact-version.jar
        //   <group-path>/<artifact>/<version>/artifact-version-sources.jar  ← same folder
        val versionDir = tempDir.resolve("com/example/foo/1.0").also { Files.createDirectories(it) }

        val binaryJar = versionDir.resolve("foo-1.0.jar")
        createJarWithClass(binaryJar)

        val sourcesJar = versionDir.resolve("foo-1.0-sources.jar")
        createJarWithSource(sourcesJar, "dev/kensa/example/KotlinInterface.kt", kotlinInterfaceSource())

        val sourceCode = sourceCodeWithJarLocation(binaryJar)
        val stream = sourceCode.sourceStreamFor(KotlinInterface::class.java)

        stream.sourceName shouldBe "/dev/kensa/example/KotlinInterface.kt"
        stream.getText(Interval.of(78, 102)) shouldBe "interface KotlinInterface"
    }

    @Test
    fun `candidateSourcesJarNames returns base variants only when no classifier present`() {
        val baseJar = Path("repo/com/example/foo/1.0/foo-1.0.jar")

        baseJar.candidateSourcesJarNames() shouldBe listOf(
            "foo-1.0-sources.jar",
            "foo-1.0-src.jar"
        )
    }

    @Test
    fun `candidateSourcesJarNames includes classifier-stripped variants for classified jar`() {
        val classifiedJar = Path("repo/com/example/foo/1.0/foo-1.0-test-support.jar")

        classifiedJar.candidateSourcesJarNames() shouldBe listOf(
            "foo-1.0-test-support-sources.jar",
            "foo-1.0-test-support-src.jar",
            "foo-1.0-sources.jar",
            "foo-1.0-src.jar"
        )
    }

    @Test
    fun `candidateSourcesJarNames handles version with hyphens`() {
        val rcJar = Path("repo/com/example/foo/1.0-RC1/foo-1.0-RC1-test-support.jar")

        rcJar.candidateSourcesJarNames() shouldBe listOf(
            "foo-1.0-RC1-test-support-sources.jar",
            "foo-1.0-RC1-test-support-src.jar",
            "foo-1.0-RC1-sources.jar",
            "foo-1.0-RC1-src.jar"
        )
    }

    @Test
    fun `can locate source for classified Maven JAR using base sources jar`() {
        // Simulates a fat sources jar: classified binary (e.g. -test-support) with a single base -sources.jar
        //   <group-path>/<artifact>/<version>/artifact-version-<classifier>.jar
        //   <group-path>/<artifact>/<version>/artifact-version-sources.jar   ← base name
        val versionDir = tempDir.resolve("com/example/foo/1.0").also { Files.createDirectories(it) }

        val binaryJar = versionDir.resolve("foo-1.0-test-support.jar")
        createJarWithClass(binaryJar)

        val sourcesJar = versionDir.resolve("foo-1.0-sources.jar")
        createJarWithSource(sourcesJar, "dev/kensa/example/KotlinInterface.kt", kotlinInterfaceSource())

        val sourceCode = sourceCodeWithJarLocation(binaryJar)
        val stream = sourceCode.sourceStreamFor(KotlinInterface::class.java)

        stream.sourceName shouldBe "/dev/kensa/example/KotlinInterface.kt"
        stream.getText(Interval.of(78, 102)) shouldBe "interface KotlinInterface"
    }

    @Test
    fun `prefers classifier-specific sources jar when one exists alongside base sources jar`() {
        val versionDir = tempDir.resolve("com/example/foo/1.0").also { Files.createDirectories(it) }

        val binaryJar = versionDir.resolve("foo-1.0-test-support.jar")
        createJarWithClass(binaryJar)

        val classifiedSourcesJar = versionDir.resolve("foo-1.0-test-support-sources.jar")
        createJarWithSource(classifiedSourcesJar, "dev/kensa/example/KotlinInterface.kt", kotlinInterfaceSource())
        val baseSourcesJar = versionDir.resolve("foo-1.0-sources.jar")
        createJarWithSource(baseSourcesJar, "dev/kensa/example/KotlinInterface.kt", "WRONG CONTENT")

        binaryJar.findSourcesJar() shouldBe classifiedSourcesJar
    }

    @Test
    fun `can locate source for classified Gradle cached JAR using base sources jar in sibling hash dir`() {
        val versionDir = tempDir.resolve("com.example/foo/1.0").also { Files.createDirectories(it) }
        val binaryJarDir = versionDir.resolve("abc123").also { Files.createDirectories(it) }
        val sourcesJarDir = versionDir.resolve("def456").also { Files.createDirectories(it) }

        val binaryJar = binaryJarDir.resolve("foo-1.0-test-support.jar")
        createJarWithClass(binaryJar)
        val sourcesJar = sourcesJarDir.resolve("foo-1.0-sources.jar")
        createJarWithSource(sourcesJar, "dev/kensa/example/KotlinInterface.kt", kotlinInterfaceSource())

        val sourceCode = sourceCodeWithJarLocation(binaryJar)
        val stream = sourceCode.sourceStreamFor(KotlinInterface::class.java)

        stream.sourceName shouldBe "/dev/kensa/example/KotlinInterface.kt"
        stream.getText(Interval.of(78, 102)) shouldBe "interface KotlinInterface"
    }

    @Test
    fun `returns null stream when no sources jar exists in Gradle cache`() {
        val versionDir = tempDir.resolve("com.example/foo/1.0").also { Files.createDirectories(it) }
        val binaryJarDir = versionDir.resolve("abc123").also { Files.createDirectories(it) }

        val binaryJar = binaryJarDir.resolve("foo-1.0.jar")
        createJarWithClass(binaryJar)

        val sourceCode = SourceCode { emptyList() }

        sourceCode.existsFor(KotlinInterface::class.java) shouldBe false
    }

    private fun kotlinInterfaceSource(): String =
        Path("src/example/kotlin/dev/kensa/example/KotlinInterface.kt").readText()

    private fun sourceCodeWithJarLocation(binaryJar: Path): SourceCode {
        val discovered = binaryJar.findSourcesJar()
            ?: error("Test setup error: could not find a sources jar alongside $binaryJar")
        return SourceCode { listOf(discovered) }
    }

    private fun createJarWithClass(path: Path) {
        ZipOutputStream(FileOutputStream(path.toFile())).use { zos ->
            zos.putNextEntry(ZipEntry("dev/kensa/example/KotlinInterface.class"))
            zos.write(ByteArray(0))
            zos.closeEntry()
        }
    }

    private fun createJarWithSource(path: Path, internalPath: String, content: String) {
        ZipOutputStream(FileOutputStream(path.toFile())).use { zos ->
            zos.putNextEntry(ZipEntry(internalPath))
            zos.write(content.toByteArray())
            zos.closeEntry()
        }
    }

    internal class InnerClass
}