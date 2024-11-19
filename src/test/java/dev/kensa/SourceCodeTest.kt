package dev.kensa

import dev.kensa.util.SourceCode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.misc.Interval
import org.junit.jupiter.api.Test

internal class SourceCodeTest {
    @Test
    fun canLocateByInnerClass() {
        val stream: CharStream = SourceCode.sourceStreamFor(InnerClass::class.java)

        stream.sourceName shouldEndWith "src/test/java/dev/kensa/SourceCodeTest.kt"
        stream.getText(Interval.of(0, 18)) shouldBe "package dev.kensa\n\n"
    }

    internal class InnerClass
}