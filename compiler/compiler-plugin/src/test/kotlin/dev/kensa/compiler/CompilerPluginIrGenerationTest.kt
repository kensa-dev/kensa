package dev.kensa.compiler

import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CompilerPluginIrGenerationTest {

    private val pluginJar: String = checkNotNull(System.getProperty("kensa.plugin.jar")) {
        "kensa.plugin.jar system property not set"
    }

    @TempDir
    lateinit var outputDir: File

    @Test
    fun `plugin handles @RenderedValue without error`() {
        compile(
            """
            package test
            import dev.kensa.RenderedValue
            class Foo {
                @RenderedValue
                fun bar(x: String): String = x
            }
            """
        ) shouldBe ExitCode.OK
    }

    @Test
    fun `plugin handles @RenderedValue expression body without error`() {
        compile(
            """
            package test
            import dev.kensa.RenderedValue
            class Foo {
                @RenderedValue
                fun bar(x: Int): Int = x + 1
            }
            """
        ) shouldBe ExitCode.OK
    }

    @Test
    fun `plugin handles @ExpandableSentence without error`() {
        compile(
            """
            package test
            import dev.kensa.ExpandableSentence
            class Foo {
                @ExpandableSentence
                fun doSomething(x: String) { println(x) }
            }
            """
        ) shouldBe ExitCode.OK
    }

    private fun compile(source: String): ExitCode {
        val sourceFile = File(outputDir, "Test.kt").also { it.writeText(source.trimIndent()) }
        val classpath = System.getProperty("java.class.path")

        return K2JVMCompiler().exec(
            System.err,
            "-classpath", classpath,
            "-Xplugin=$pluginJar",
            "-Xskip-prerelease-check",
            "-no-stdlib",
            "-d", outputDir.absolutePath,
            sourceFile.absolutePath
        )
    }
}
