package dev.kensa.example

import dev.kensa.RenderedValue
import dev.kensa.RenderedValueContainer

class KotlinWithContainerChains {

    class Stub {
        fun sends(r: Request): Action = Action()
    }

    data class UseCase(@RenderedValue val stub: Stub, val plain: String, @RenderedValue val ref: Ref)
    class Ref(val name: String = "REF-1")
    class Request
    class Action

    @RenderedValueContainer
    private val heldCase = UseCase(Stub(), "p", Ref())

    private fun whenever(a: Action) {}
    private fun sendIt(a: Any?) {}
    private fun aRequest(): Request = Request()

    fun chainWithTrailingCall(@RenderedValueContainer useCase: UseCase) {
        whenever(useCase.stub.sends(aRequest()))
    }

    fun chainWithoutTrailingCall(@RenderedValueContainer useCase: UseCase) {
        sendIt(useCase.stub)
    }

    fun multiSegmentPath(@RenderedValueContainer useCase: UseCase) {
        sendIt(useCase.ref.name)
    }

    fun unannotatedMember(@RenderedValueContainer useCase: UseCase) {
        sendIt(useCase.plain)
    }

    fun callableReference(@RenderedValueContainer useCase: UseCase) {
        sendIt(useCase::stub)
    }

    fun safeNavChain(@RenderedValueContainer useCase: UseCase) {
        sendIt(useCase?.stub)
    }

    fun notNullChain(@RenderedValueContainer useCase: UseCase) {
        sendIt(useCase.stub!!)
    }

    fun fieldContainerChain() {
        whenever(heldCase.stub.sends(aRequest()))
    }

    @RenderedValue
    private fun theWholesaler(useCase: UseCase): Stub = useCase.stub

    fun renderedValueCallWithTrailingChain(useCase: UseCase) {
        whenever(theWholesaler(useCase).sends(aRequest()))
    }
}
