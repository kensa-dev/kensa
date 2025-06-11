package dev.kensa.parse

import dev.kensa.fixture.Fixture
import dev.kensa.fixture.FixtureRegistry
import dev.kensa.fixture.Fixtures
import dev.kensa.util.resolvePath

class FixturesAccessor(private val fixtures: Fixtures) {

    fun valueOf(fixtureName: String, path: String?): Any? {
        val key: Fixture<Any> = FixtureRegistry.lookupFixture(fixtureName)

        return resolvePath(fixtures[key], path)
    }
}