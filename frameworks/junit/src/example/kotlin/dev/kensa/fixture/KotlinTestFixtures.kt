package dev.kensa.fixture

object KotlinTestFixtures : FixtureContainer {
    private val stringFixtures = mutableListOf("parent1", "parent2", "parent3", "parent4")
    private val childStringFixtures = mutableListOf("child1", "child2", "child3", "child4")

    val StringFixture = fixture("KotlinStringFixture") { stringFixtures.removeFirst() }
    val ChildStringFixture = fixture("KotlinChildStringFixture", StringFixture) { "${it}_${childStringFixtures.removeFirst()}" }

    private val PrivateFixture = fixture("KotlinPrivateFixture") { 111 }
    val PublicFixture = fixture("KotlinPublicFixture", PrivateFixture) { 111 + it }
}