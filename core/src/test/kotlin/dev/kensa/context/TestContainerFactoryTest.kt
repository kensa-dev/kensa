@file:OptIn(dev.kensa.KensaExperimental::class)

package dev.kensa.context

import dev.kensa.Configuration
import dev.kensa.Epic
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

    @Epic("EPIC-1", "EPIC-2")
    private class EpicSample {
        @Epic("EPIC-3")
        fun methodWithEpic() = Unit
        fun methodWithoutEpic() = Unit
    }

    private fun factoryFor(vararg methods: String) = TestContainerFactory(
        initialStateFor = { TestState.NotExecuted },
        displayNameFor = { null },
        findTestMethods = { cls -> methods.map { cls.getDeclaredMethod(it) }.toSet() },
        testInvocationFactory = mock(),
        configuration = Configuration(),
    )

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

    @Test fun `populates class epics from Epic annotation`() {
        val container = factoryFor("methodWithEpic").createFor(EpicSample::class.java, "EpicSample")
        container.epics shouldBe listOf("EPIC-1", "EPIC-2")
    }

    @Test fun `populates method epics from Epic annotation`() {
        val container = factoryFor("methodWithEpic", "methodWithoutEpic").createFor(EpicSample::class.java, "EpicSample")
        container.methodContainers.getValue(EpicSample::class.java.getDeclaredMethod("methodWithEpic")).epics shouldBe listOf("EPIC-3")
        container.methodContainers.getValue(EpicSample::class.java.getDeclaredMethod("methodWithoutEpic")).epics shouldBe emptyList()
    }

    @Test fun `epics are empty without annotation`() {
        val container = factoryFor("provideFlow").createFor(Sample::class.java, "Sample")
        container.epics shouldBe emptyList()
    }
}
