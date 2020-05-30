package dev.kensa.context

import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.GivensWithInteractionsBuilder
import dev.kensa.StateExtractor
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
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
        try {
            interactions.setUnderTest(true)
            action.execute(givens, interactions)
        } finally {
            interactions.setUnderTest(false)
        }
    }

    fun <A, T> then(extractor: StateExtractor<T>, assertProvider: (T?) -> A): A = try {
        interactions.setUnderTest(true)
        assertProvider(extractor.execute(interactions))
    } finally {
        interactions.setUnderTest(false)
    }

    fun <T> then(extractor: StateExtractor<T?>, matcher: Matcher<in T?>) = try {
        interactions.setUnderTest(true)
        MatcherAssert.assertThat(extractor.execute(interactions), matcher)
    } finally {
        interactions.setUnderTest(false)
    }

    fun disableInteractionTestGroup() {
        interactions.disableUnderTest()
    }
}