package dev.kensa.fixture


object KotlinFixtureSuite : FixtureSuite {
    override val containers = listOf(KotlinTestFixtures, SuiteIntFixtures)
}

object SuiteIntFixtures : FixtureContainer {
    val SuiteIntFixture = fixture("SuiteInt") { 42 }
}

object KotlinTestFixtures : FixtureContainer {
    private val stringFixtures = mutableListOf("parent1", "parent2", "parent3", "parent4", "parent5", "parent6")
    private val childStringFixtures = mutableListOf("child1", "child2", "child3", "child4", "child5", "child6")

    val StringFixture = fixture("KotlinStringFixture", highlighted = true) { stringFixtures.removeFirst() }
    val ChildStringFixture = fixture("KotlinChildStringFixture", StringFixture) { "${it}_${childStringFixtures.removeFirst()}" }

    private val PrivateFixture = fixture("KotlinPrivateFixture") { 111 }
    val PublicFixture = fixture("KotlinPublicFixture", PrivateFixture) { 111 + it }
}