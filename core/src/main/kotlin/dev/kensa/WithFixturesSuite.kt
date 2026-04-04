package dev.kensa

import dev.kensa.fixture.Fixture
import dev.kensa.fixture.FixtureContainer

interface WithFixturesSuite<F : FixtureContainer> : WithFixturesAndOutputs {
    val fixturesObject: F
    fun <T> fixtures(block: F.() -> Fixture<T>): T = fixtures(fixturesObject.block())
}
