package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.Fixture
import dev.kensa.StateCollector
import dev.kensa.example.MyFactoryFixtures.myFixture
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.fixture
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class KotlinWithFixtureFactoryTest : KotlinExampleTest(), WithHamkrest {

    init {
        registerFixtures(MyFactoryFixtures)
    }

    @ParameterizedTest
    @CsvSource("Meh,Wow", "Foo,Bar")
    fun rendersTheSeededFixtureFactoryValuesPerParameter(p1: String, p2: String) {
        then(greetingFor(p1), equalTo(fixtures[myFixture(p1)]))
        then(greetingFor(p2), equalTo(fixtures[myFixture(p2)]))
        then(greetingFor(p1), equalTo(fixtures[myFixture(p1)]))
    }

    private fun greetingFor(param: String) = StateCollector { _ -> if (param == "Meh") "Bah" else "Yay" }
}

object MyFactoryFixtures : FixtureContainer {
    @Fixture("MyFixture")
    fun myFixture(param: String) = fixture { if (param == "Meh") "Bah" else "Yay" }
}
