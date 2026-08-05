package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import org.junit.jupiter.api.Test

enum class OrderState { PENDING, COMPLETE }
enum class PaymentState { PENDING, SETTLED }

class KotlinWithQualifiedConstantsTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        given(theOrderIs(OrderState.PENDING))

        whenever(thePaymentBecomes(PaymentState.PENDING))
    }

    private fun theOrderIs(state: OrderState) = Action<GivensContext> {}

    private fun thePaymentBecomes(state: PaymentState) = Action<ActionContext> {}
}
