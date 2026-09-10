package dev.kensa.example

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class KotlinWithPollingBlocks {

    fun eventuallyBlock() {
        thenEventually(2.seconds) {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    fun continuallyBlock() {
        thenContinually(2.seconds) {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    fun singleCollectorBlock() {
        thenEventually(theFirstValue()) {
            andThenSomething()
        }
    }

    fun lazyStepInsideBlock() {
        thenEventually(2.seconds) {
            then { theFirstSpec() }
            and(theSecondValue(), equalTo("second"))
        }
    }

    fun threeStepBlock() {
        thenEventually(2.seconds) {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
            and(theThirdValue(), equalTo("third"))
        }
    }

    fun nonStepFirstStatementBlock() {
        thenEventually(2.seconds) {
            val order = fetchOrder()
            then(order, equalTo("X"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    fun zeroArgEventuallyBlock() {
        thenEventually {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    fun andEventuallyBlock() {
        andEventually(2.seconds) {
            then(theFirstValue(), equalTo("first"))
            and(theSecondValue(), equalTo("second"))
        }
    }

    class Steps {
        fun <T> then(value: T, matcher: T) {}
        fun <T> and(value: T, matcher: T) {}
        fun <T> then(spec: () -> T) {}
    }

    private fun thenEventually(duration: Duration, block: Steps.() -> Unit) {}
    private fun thenContinually(duration: Duration, block: Steps.() -> Unit) {}
    private fun thenEventually(block: Steps.() -> Unit) {}
    private fun andEventually(duration: Duration, block: Steps.() -> Unit) {}
    private fun <T> thenEventually(collector: T, block: T.() -> Unit) {}
    private fun fetchOrder() = "X"
    private fun theFirstValue() = "first"
    private fun theSecondValue() = "second"
    private fun theThirdValue() = "third"
    private fun theFirstSpec() = "first"
    private fun <T> equalTo(value: T) = value
    private fun String.andThenSomething() {}
}
