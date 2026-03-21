package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.startsWith
import dev.kensa.Action
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.example.MoreKotlinTestFixtures.BooleanFixture
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.KotlinTestFixtures
import dev.kensa.fixture.KotlinTestFixtures.ChildStringFixture
import dev.kensa.fixture.KotlinTestFixtures.PublicFixture
import dev.kensa.fixture.KotlinTestFixtures.StringFixture
import dev.kensa.fixture.fixture
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

class KotlinWithFixturesTest : KotlinExampleTest(), WithHamkrest {

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

    @Test
    fun test4() {
        then(thePrivateSourcedFixture(), equalTo(fixtures[PublicFixture]))
    }

    @Test
    fun test5() {
        then(theConcatenatedFixtures(), equalTo(concatenate(fixtures(StringFixture), fixtures(BooleanFixture))))
    }

    @RenderedValue
    private fun concatenate(string: String, boolean: Boolean) = "$string-$boolean"
    private fun theConcatenatedFixtures() = StateCollector { concatenate(fixtures(StringFixture), fixtures(BooleanFixture)) }

    private fun theBooleanFixture(): StateCollector<Boolean> = StateCollector { fixtures[BooleanFixture] }
    private fun theIntegerFixture(): StateCollector<Int> = StateCollector { fixtures[IntegerFixture] }
    private fun theStringFixture(): StateCollector<String> = StateCollector { fixtures[StringFixture] }
    private fun thePrivateSourcedFixture(): StateCollector<Int> = StateCollector { fixtures[PublicFixture] }
    private fun theFixture(value: String): StateCollector<String> = StateCollector { value }
    private fun somePrerequisites(): Action<GivensContext> = Action { }

    companion object : FixtureContainer {
        val IntegerFixture = fixture("KotlinIntegerFixture") { 23 }
    }
}

object MoreKotlinTestFixtures : FixtureContainer {
    val BooleanFixture = fixture("KotlinBooleanFixture") { false }
}
