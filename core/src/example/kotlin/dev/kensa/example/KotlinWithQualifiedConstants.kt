package dev.kensa.example

import dev.kensa.ExpandableSentence

enum class OrderStatus { PENDING, COMPLETE }
enum class PaymentStatus { PENDING, SETTLED }

sealed class DeliveryStatus {
    data object Pending : DeliveryStatus()
    data object Shipped : DeliveryStatus()
}

class Limits { companion object { const val MAX = 10 } }

class KotlinWithQualifiedConstants {

    fun testWithQualifiedEnums() {
        theOrderIs(OrderStatus.PENDING)
        andThePaymentIs(PaymentStatus.PENDING)
    }

    fun testWithSealedDataObject() {
        theDeliveryIs(DeliveryStatus.Pending)
    }

    fun testWithCompanionConst() {
        theLimitIs(Limits.MAX)
    }

    fun testWithEnumMethodCall() {
        theOrderIs(OrderStatus.valueOf("PENDING"))
    }

    fun testWithExpandableArgument() {
        orderBecomes(OrderStatus.PENDING)
    }

    @ExpandableSentence
    private fun orderBecomes(status: OrderStatus) {}

    private fun theOrderIs(status: OrderStatus) {}
    private fun andThePaymentIs(status: PaymentStatus) {}
    private fun theDeliveryIs(status: DeliveryStatus) {}
    private fun theLimitIs(limit: Int) {}
}
