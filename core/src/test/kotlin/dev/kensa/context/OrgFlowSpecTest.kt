package dev.kensa.context

import dev.kensa.OrgFlow
import dev.kensa.OrgFlowMarker
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

enum class SampleFlow(
    override val category: String,
    override val flowName: String,
    override val attributes: Map<String, String> = emptyMap(),
) : OrgFlowSpec {
    PriceAndReserve("Checkout", "Price and Reserve", mapOf("product" to "FTTP")),
}

@OrgFlowMarker
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class SampleFlowRef(val value: SampleFlow)

class OrgFlowSpecTest {
    private class Sample {
        @OrgFlow(category = "Provide", name = "Provide with Cancel", product = "FTTP")
        fun annotated() = Unit
        fun plain() = Unit
    }

    private class MetaSample {
        @SampleFlowRef(SampleFlow.PriceAndReserve)
        fun annotated() = Unit
    }

    @Test fun `maps the OrgFlow annotation to a spec with product folded into attributes`() {
        orgFlowOf(Sample::class.java.getDeclaredMethod("annotated")) shouldBe
            SimpleOrgFlowSpec("Provide", "Provide with Cancel", mapOf("product" to "FTTP"))
    }

    @Test fun `returns null for an unannotated method`() {
        orgFlowOf(Sample::class.java.getDeclaredMethod("plain")).shouldBeNull()
    }

    @Test fun `resolves a typed enum flow via a meta-annotated marker`() {
        orgFlowOf(MetaSample::class.java.getDeclaredMethod("annotated")) shouldBe SampleFlow.PriceAndReserve
    }
}
