package dev.kensa.parse

import dev.kensa.fixture.Fixture
import dev.kensa.fixture.FixtureRegistry
import dev.kensa.fixture.Fixtures

class FixturesAccessor(private val fixtures: Fixtures) {

    fun valueOf(fixtureName: String): Any? {
        val key: Fixture<Any> = FixtureRegistry.lookupFixture(fixtureName)

        return fixtures[key]
    }
}