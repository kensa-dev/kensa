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

    @ParameterizedTest
    @CsvSource("Meh", "Foo")
    fun rendersTheNavigatedFactoryFixtureProperty(p1: String) {
        then(productNameFor(p1), equalTo(fixtures[MyFactoryFixtures.productFor(p1)].name))
    }

    private fun greetingFor(param: String) = StateCollector { _ -> if (param == "Meh") "Bah" else "Yay" }

    private fun productNameFor(param: String) = StateCollector { _ -> "product-$param" }
}

object MyFactoryFixtures : FixtureContainer {
    @Fixture("MyFixture")
    fun myFixture(param: String) = fixture { if (param == "Meh") "Bah" else "Yay" }

    @Fixture("ProductFixture")
    fun productFor(param: String) = fixture { FactoryProduct("product-$param", 10) }
}

data class FactoryProduct(val name: String, val price: Int)
