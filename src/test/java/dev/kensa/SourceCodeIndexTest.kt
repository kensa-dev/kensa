package dev.kensa

import dev.kensa.util.SourceCodeIndex.Companion.locate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class SourceCodeIndexTest {
    @Test
    fun canLocateByInnerClass() {
        val path = locate(InnerClass::class)
        Assertions.assertThat(path).isNotNull()
    }

    internal class InnerClass
}