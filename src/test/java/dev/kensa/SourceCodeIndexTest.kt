package dev.kensa

import dev.kensa.util.SourceCodeIndex.locate
import io.kotest.matchers.string.shouldEndWith
import org.junit.jupiter.api.Test
import kotlin.io.path.absolutePathString

internal class SourceCodeIndexTest {
    @Test
    fun canLocateByInnerClass() {
        locate(InnerClass::class.java).absolutePathString() shouldEndWith "/src/test/java/dev/kensa/SourceCodeIndexTest.kt"
    }

    internal class InnerClass
}