package dev.kensa.fixture

interface FixtureSuite : FixtureContainer {
    val containers: List<FixtureContainer>
}
