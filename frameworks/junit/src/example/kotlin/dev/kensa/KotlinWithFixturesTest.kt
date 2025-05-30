package dev.kensa

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.startsWith
import dev.kensa.MoreKotlinTestFixtures.BooleanFixture
import dev.kensa.KotlinTestFixtures.ChildStringFixture
import dev.kensa.KotlinTestFixtures.StringFixture
import dev.kensa.fixture.*
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

class KotlinWithFixturesTest : KensaTest, WithHamkrest {

    init {
        registerFixtures(KotlinTestFixtures, MoreKotlinTestFixtures, Companion)
    }

    @Test
    fun test1() {
        given(somePrerequisites())

        then(theStringFixture(), equalTo(fixtures[StringFixture]))
        then(theBooleanFixture(), equalTo(fixtures[BooleanFixture]))
    }

    @Test
    fun test2() {
        given(somePrerequisites())

        then(theStringFixture(), equalTo(fixtures[StringFixture]))
        then(theFixture(fixtures[ChildStringFixture]), startsWith(fixtures[StringFixture]))
    }

    @Test
    fun test3() {
        then(theIntegerFixture(), equalTo(fixtures[IntegerFixture]))
    }

    private fun theBooleanFixture(): StateExtractor<Boolean> = StateExtractor { interactions: CapturedInteractions -> fixtures[BooleanFixture] }
    private fun theIntegerFixture(): StateExtractor<Int> = StateExtractor { interactions: CapturedInteractions -> fixtures[IntegerFixture] }
    private fun theStringFixture(): StateExtractor<String> = StateExtractor { interactions: CapturedInteractions -> fixtures[StringFixture] }
    private fun theFixture(value: String): StateExtractor<String> = StateExtractor { interactions: CapturedInteractions -> value }
    private fun somePrerequisites() = GivensBuilder { givens: Givens, _ -> givens.put("foo", fixtures[StringFixture]) }

    companion object : FixtureContainer {
        val IntegerFixture = fixture("KotlinIntegerFixture") { 23 }
    }
}

object KotlinTestFixtures : FixtureContainer {
    private val stringFixtures = mutableListOf("parent1", "parent2")
    private val childStringFixtures = mutableListOf("child1", "child2")

    val StringFixture = fixture("KotlinStringFixture") { stringFixtures.removeFirst() }
    val ChildStringFixture = fixture("KotlinChildStringFixture", StringFixture) { "${it}_${childStringFixtures.removeFirst()}" }
}

object MoreKotlinTestFixtures : FixtureContainer {
    val BooleanFixture = fixture("KotlinBooleanFixture") { false }
}