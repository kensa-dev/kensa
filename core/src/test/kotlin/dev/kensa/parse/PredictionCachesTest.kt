package dev.kensa.parse

import dev.kensa.parse.kotlin.KensaKotlinLexer
import dev.kensa.parse.kotlin.KotlinParser
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test

class PredictionCachesTest {

    private val source = """
        class Sample {
            fun answer(): Int = listOf(1, 2, 3).map { it * 2 }.sum()
        }
    """.trimIndent()

    private fun parser() = KotlinParser(CommonTokenStream(KensaKotlinLexer(CharStreams.fromString(source))))

    private val KotlinParser.states get() = interpreter.decisionToDFA.sumOf { it.states.size }

    @Test
    fun `a parse fills the attached caches and leaves the generated parser's static ones alone`() {
        val generated = parser().interpreter
        val parser = PredictionCaches().attachTo(parser())

        parser.kotlinFile()

        parser.interpreter.decisionToDFA shouldNotBeSameInstanceAs generated.decisionToDFA
        parser.interpreter.sharedContextCache shouldNotBeSameInstanceAs generated.sharedContextCache
        parser.states shouldBeGreaterThan 0
        parser.numberOfSyntaxErrors shouldBe 0
    }

    @Test
    fun `parsers share the caches while they are under the limit`() {
        val caches = PredictionCaches()
        val first = caches.attachTo(parser()).apply { kotlinFile() }
        val second = caches.attachTo(parser())

        second.interpreter.decisionToDFA shouldBeSameInstanceAs first.interpreter.decisionToDFA
        second.interpreter.sharedContextCache shouldBeSameInstanceAs first.interpreter.sharedContextCache
    }

    @Test
    fun `caches over the limit are replaced with empty ones`() {
        val caches = PredictionCaches(maxStates = 1)
        val first = caches.attachTo(parser()).apply { kotlinFile() }
        val second = caches.attachTo(parser())

        first.states shouldBeGreaterThan 1
        second.interpreter.decisionToDFA shouldNotBeSameInstanceAs first.interpreter.decisionToDFA
        second.interpreter.sharedContextCache shouldNotBeSameInstanceAs first.interpreter.sharedContextCache
        second.states shouldBe 0
    }
}
