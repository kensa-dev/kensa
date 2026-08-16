package dev.kensa

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

class OptInMarkersTest {

    @TempDir
    lateinit var tempDir: Path

    @Nested
    inner class ExperimentalMarker {

        @Test
        fun `using an experimental API without opt-in warns but still compiles`() {
            val diagnostics = compile(
                """
                package sample
                import dev.kensa.context.OrgFlowSpec
                fun describe(spec: OrgFlowSpec) = spec
                """.trimIndent()
            )

            diagnostics.errors().shouldBeEmpty()
            diagnostics.warnings().joinToString("\n") shouldContain "experimental"
        }

        @Test
        fun `using an experimental API with opt-in produces no opt-in warning`() {
            val diagnostics = compile(
                """
                @file:OptIn(dev.kensa.KensaExperimental::class)
                package sample
                import dev.kensa.context.OrgFlowSpec
                fun describe(spec: OrgFlowSpec) = spec
                """.trimIndent()
            )

            diagnostics.errors().shouldBeEmpty()
            diagnostics.warnings().none { it.contains("experimental") } shouldBe true
        }
    }

    @Nested
    inner class InternalMarker {

        @Test
        fun `using an internal API without opt-in fails to compile`() {
            val diagnostics = compile(
                """
                package sample
                import dev.kensa.context.FrameworkDescriptor
                fun describe(descriptor: FrameworkDescriptor) = descriptor
                """.trimIndent()
            )

            diagnostics.errors().joinToString("\n") shouldContain "Kensa internal API"
        }

        @Test
        fun `using an internal API with opt-in compiles`() {
            val diagnostics = compile(
                """
                @file:OptIn(dev.kensa.KensaInternalApi::class)
                package sample
                import dev.kensa.context.FrameworkDescriptor
                fun describe(descriptor: FrameworkDescriptor) = descriptor
                """.trimIndent()
            )

            diagnostics.errors().shouldBeEmpty()
        }

        @Test
        fun `opting in to the experimental marker does not unlock internal API`() {
            val diagnostics = compile(
                """
                @file:OptIn(dev.kensa.KensaExperimental::class)
                package sample
                import dev.kensa.context.FrameworkDescriptor
                fun describe(descriptor: FrameworkDescriptor) = descriptor
                """.trimIndent()
            )

            diagnostics.errors().joinToString("\n") shouldContain "Kensa internal API"
        }
    }

    @Nested
    inner class StableSurface {

        @Test
        fun `registering a tab service needs no opt-in`() {
            val diagnostics = compile(
                """
                package sample
                import dev.kensa.Configuration
                fun register(configuration: Configuration) {
                    configuration.registerTabService(String::class) { "a service" }
                }
                """.trimIndent()
            )

            diagnostics.errors().shouldBeEmpty()
            diagnostics.warnings().none { it.contains("opt-in") } shouldBe true
        }
    }

    private fun compile(source: String): Diagnostics {
        val sourceFile = tempDir.resolve("Sample.kt").also { it.writeText(source) }
        val collector = Diagnostics()
        val arguments = K2JVMCompilerArguments().apply {
            freeArgs = listOf(sourceFile.toAbsolutePath().toString())
            classpath = System.getProperty("java.class.path")
            destination = tempDir.resolve("out").toAbsolutePath().toString()
            noStdlib = true
            noReflect = true
        }
        K2JVMCompiler().exec(collector, Services.EMPTY, arguments)
        return collector
    }

    private class Diagnostics : MessageCollector {
        private val entries = mutableListOf<Pair<CompilerMessageSeverity, String>>()
        override fun clear() = entries.clear()
        override fun hasErrors() = entries.any { it.first.isError }
        override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
            entries.add(severity to message)
        }

        fun errors() = entries.filter { it.first.isError }.map { it.second }
        fun warnings() = entries.filter { it.first == CompilerMessageSeverity.WARNING || it.first == CompilerMessageSeverity.STRONG_WARNING }.map { it.second }
    }
}
