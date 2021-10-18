package dev.kensa.context

import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.GivensWithInteractionsBuilder
import dev.kensa.StateExtractor
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert

class TestContext(val givens: Givens, val interactions: CapturedInteractions) {
    fun given(builder: GivensWithInteractionsBuilder) {
        builder.build(givens, interactions)
    }

    fun given(builder: GivensBuilder) {
        builder.build(givens)
    }

    fun whenever(action: ActionUnderTest) {
        interactions.isUnderTest = true
        action.execute(givens, interactions)
    }

    fun <A, T> then(extractor: StateExtractor<T>, assertProvider: (T?) -> A): A =
        assertProvider(extractor.execute(interactions))

    fun <A, T> thenEventually(extractor: StateExtractor<T>, assertProvider: (T?) -> A): A {
        await untilAsserted { assertProvider(extractor.execute(interactions)) }
        return assertProvider(extractor.execute(interactions))
    }

    fun <T> then(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) {
        MatcherAssert.assertThat(extractor.execute(interactions), matcher)
    }

    fun <T> thenEventually(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) {
        await untilAsserted { MatcherAssert.assertThat(extractor.execute(interactions), matcher) }
        MatcherAssert.assertThat(extractor.execute(interactions), matcher)
    }

    fun disableInteractionTestGroup() {
        interactions.disableUnderTest()
    }
}