package dev.kensa.context

import dev.kensa.Configuration
import dev.kensa.OrgFlow
import dev.kensa.state.TestState
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class TestContainerFactoryTest {
    private class Sample {
        @OrgFlow(category = "Provide", name = "Provide with Cancel", product = "FTTP")
        fun provideFlow() = Unit
    }

    @Test fun `populates orgFlow from OrgFlow annotation`() {
        val factory = TestContainerFactory(
            initialStateFor = { TestState.NotExecuted },
            displayNameFor = { null },
            findTestMethods = { setOf(it.getDeclaredMethod("provideFlow")) },
            testInvocationFactory = mock(),
            configuration = Configuration(),
        )

        val container = factory.createFor(Sample::class.java, "Sample")

        container.methodContainers.values.single().orgFlow shouldBe
            SimpleOrgFlowSpec("Provide", "Provide with Cancel", mapOf("product" to "FTTP"))
    }
}
