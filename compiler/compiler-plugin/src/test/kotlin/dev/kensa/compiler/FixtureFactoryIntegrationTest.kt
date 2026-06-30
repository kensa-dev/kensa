package dev.kensa.compiler

import dev.kensa.fixture.Fixtures
import example.FactoryFixtures
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FixtureFactoryIntegrationTest {

    @Test
    fun `rewrites the no-name fixture to a keyed factoryFixture with the annotation key and identity args`() {
        val fixture = FactoryFixtures.myFixture("Meh")

        fixture.key shouldBe "MyFixture(Meh)"
        Fixtures()[fixture] shouldBe "Bah"
    }

    @Test
    fun `distinct identity args produce distinct composite keys`() {
        FactoryFixtures.myFixture("Wow").key shouldBe "MyFixture(Wow)"
    }

    @Test
    fun `injects all factory value parameters as identity args in order`() {
        val fixture = FactoryFixtures.twoArg("a", 7)

        fixture.key shouldBe "TwoArg(a, 7)"
        Fixtures()[fixture] shouldBe "a-7"
    }
}
