package dev.kensa

import dev.kensa.util.SourceCodeIndex.locate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.absolutePathString

internal class SourceCodeIndexTest {
    @Test
    fun canLocateByInnerClass() {
        assertThat(locate(InnerClass::class.java).absolutePathString()).endsWith("/src/test/java/dev/kensa/SourceCodeIndexTest.kt")
    }

    internal class InnerClass
}