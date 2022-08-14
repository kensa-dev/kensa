package dev.kensa.context

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TestContainerTest {
    @Test
    fun `writes test file on close`() {
        val writer = mock<(TestContainer) -> Unit>()

        val container = TestContainer(
            javaClass,
            "Display Name",
            emptyMap(),
            null,
            emptyList(),
            writer
        )

        container.close()

        verify(writer).invoke(container)
    }
}