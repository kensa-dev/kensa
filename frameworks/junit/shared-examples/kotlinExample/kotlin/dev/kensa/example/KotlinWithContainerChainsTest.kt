package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.RenderedValueContainer
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.fixture
import dev.kensa.fixture.fixtures
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class KotlinWithContainerChainsTest : KotlinExampleTest() {

    @RenderedValueContainer
    private val heldWholesaler = WholesalerUseCase(WholesalerStub("giffgaff"), "APAC")

    @ParameterizedTest
    @MethodSource("wholesalers")
    fun canCheckSession(@RenderedValueContainer useCase: WholesalerUseCase) {
        given(targeting(useCase.market))

        whenever(useCase.stub.sends(aCheckSessionRequest()))
    }

    @Test
    fun canCheckSessionForHeldWholesaler() {
        given(withReference(heldWholesaler.reference))

        whenever(heldWholesaler.stub.sends(aCheckSessionRequest()))
    }

    private fun targeting(market: String) = Action<GivensContext> {}

    private fun withReference(reference: String) = Action<GivensContext> {}

    private fun aCheckSessionRequest() = CheckSessionRequest()

    companion object {
        @JvmStatic
        fun wholesalers() = listOf(
            WholesalerUseCase(WholesalerStub("fastweb"), "EMEA"),
            WholesalerUseCase(WholesalerStub("vodafone"), "APAC")
        )
    }
}

class WholesalerStub(val name: String) {
    fun sends(request: CheckSessionRequest): Action<ActionContext> = Action {}
    override fun toString() = "WholesalerStub"
}

object WholesalerFixtures : FixtureContainer {
    val WholesalerReferenceFx = fixture("WholesalerReferenceFx") { "REF-DEFAULT" }
}

data class WholesalerUseCase(@RenderedValue val stub: WholesalerStub, val market: String) {
    @get:RenderedValue
    val reference: String by fixtures(WholesalerFixtures.WholesalerReferenceFx)
}

class CheckSessionRequest
