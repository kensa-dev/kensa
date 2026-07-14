package dev.kensa.example

import dev.kensa.RenderedValue

class KotlinWithFixtureSubscript {

    class Holder(val stringValue: String)

    class FixtureMap {
        operator fun get(key: String) = Holder("x")
    }

    class Field
    class Matcher {
        fun and(other: Matcher): Matcher = this
    }

    class OutputsMap {
        operator fun get(key: String) = Holder("x")
    }

    class Orchestration {
        fun receivesANotification(matcher: Matcher) = Matcher()
    }

    @RenderedValue
    private val trackingId = "TRK"

    private val outputs = OutputsMap()
    private val fixtures = FixtureMap()
    private val aProvider = Field()
    private val aProduct = Field()
    private val aType = Field()
    private val orchestration = Orchestration()

    private infix fun Field.withValue(value: String): Matcher = Matcher()
    private infix fun Field.of(value: Any?): Matcher = Matcher()
    private fun thatHas(matcher: Matcher): Matcher = matcher
    private fun matchesTheNotificationSchema(): Matcher = Matcher()
    private fun thenEventually(matcher: Matcher) {}
    private fun given(matcher: Matcher) {}

    fun rendersFactoryInSubscript(@RenderedValue provideType: String) {
        given(aProduct of fixtures[myFixture(provideType)])
    }

    fun handlesOrderAcknowledgedNotification(@RenderedValue provideType: String) {
        thenEventually(orchestration.receivesANotification(
            thatHas(
                (aProvider withValue "FASTWEB")
                    .and(aType of "ProvideAcknowledged")
                    .and(aProduct withValue fixtures[myFixture(provideType)].stringValue)
            ).and(matchesTheNotificationSchema())
        ))
    }

    fun handlesOutputsNotification() {
        thenEventually(orchestration.receivesANotification(
            thatHas(
                (aProvider withValue "FASTWEB")
                    .and(aType of "ProvideAcknowledged")
                    .and(aProduct withValue outputs[trackingId].stringValue)
            ).and(matchesTheNotificationSchema())
        ))
    }

    private fun myFixture(arg: String) = "MyFixture"
}
