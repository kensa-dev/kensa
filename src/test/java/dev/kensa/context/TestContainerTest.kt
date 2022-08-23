package dev.kensa.context

import dev.kensa.output.TestWriter
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TestContainerTest {
    @Test
    fun `writes test file on close`() {
        val writer = mock<TestWriter>()

        val container = TestContainer(
            javaClass,
            "Display Name",
            emptyMap(),
            null,
            emptyList(),
            writer
        )

        container.close()

        verify(writer).write(container)
    }
}