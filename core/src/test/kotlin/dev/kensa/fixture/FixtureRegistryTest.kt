package dev.kensa.fixture

import dev.kensa.fixture.FixtureRegistry.clearFixtures
import dev.kensa.fixture.FixtureRegistry.lookupFixture
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

class FixtureRegistryTest {

    @BeforeEach
    fun setup() {
        clearFixtures()
    }

    @Test
    fun `should register and lookup fixture keys by variable name`() {
        registerFixtures(TestFixtureObject, TestFixtureContainer.Companion)

        // Inside Companion Object
        lookupFixture<LocalDate>("PrivateFixture").shouldNotBeNull().should {
            it.key shouldBe "PrivateFixture"
            it.shouldBeInstanceOf<PrimaryFixture<String>>()
        }

        // Inside Object (These private properties require a different mechanism to get the value successfully)
        lookupFixture<LocalDate>("PrivateFixture2").shouldNotBeNull().should {
            it.key shouldBe "PrivateFixture2"
            it.shouldBeInstanceOf<PrimaryFixture<String>>()
        }

        lookupFixture<LocalDate>("TestDateFixture").shouldNotBeNull().should {
            it.key shouldBe "TestDate"
            it.shouldBeInstanceOf<PrimaryFixture<LocalDate>>()
        }

        lookupFixture<DayOfWeek>("TestDayFixture").shouldNotBeNull().should {
            it.key shouldBe "TestDay"
            it.shouldBeInstanceOf<SecondaryFixture<DayOfWeek>>()
        }

        lookupFixture<Duration>("TestDurationFixture").shouldNotBeNull().should {
            it.key shouldBe "TestDuration"
            it.shouldBeInstanceOf<PrimaryFixture<Duration>>()
        }
    }

    @Test
    fun `should lookup fixture keys from Java classes`() {
        FixtureRegistry.register(JavaFixtureClass::class.java)

        lookupFixture<Duration>("DURATION_FIXTURE").shouldNotBeNull().should {
            it.key shouldBe "JavaDurationFixture"
            it.shouldBeInstanceOf<PrimaryFixture<Duration>>()
        }

        lookupFixture<Duration>("PRIVATE_FIXTURE").shouldNotBeNull().should {
            it.key shouldBe "JavaPrivateFixture"
            it.shouldBeInstanceOf<PrimaryFixture<Integer>>()
        }
    }

    @Test
    fun `should throw exception when registering fixture with duplicate key`() {
        FixtureRegistry.register(TestFixtureObject::class.java)

        shouldThrow<IllegalStateException> {
            FixtureRegistry.register(TestFixtureObject1::class.java)
        }.shouldHaveMessage("Duplicate fixture key: TestDuration. A fixture with this key is already registered with name: TestDurationFixture")
    }

    @Test
    fun `should throw exception when registering fixture with duplicate field name`() {
        FixtureRegistry.register(FixtureContainerWithDuplicatePropertyName::class.java)

        shouldThrow<IllegalStateException> {
            FixtureRegistry.register(FixtureContainerWithDuplicateProperty1::class.java)
        }.shouldHaveMessage("Duplicate fixture (field/property) name: DuplicateNameFixture. A fixture with this name is already registered with key: FirstKey")
    }

    @Suppress("unused")
    class TestFixtureContainer {
        companion object : FixtureContainer {
            private val PrivateFixture = fixture<String>("PrivateFixture") { "Private Fixture" }
            val PublicFixture = fixture<String, String>("PublicFixture", PrivateFixture) { "Public Fixture based on $it" }
            val TestDateFixture = fixture<LocalDate>("TestDate") { LocalDate.now() }
            val TestDayFixture = fixture<DayOfWeek, LocalDate>("TestDay", TestDateFixture) { date -> date.dayOfWeek }
        }
    }

    @Suppress("unused")
    object TestFixtureObject : FixtureContainer {
        private val PrivateFixture2 = fixture<String>("PrivateFixture2") { "Private Fixture 2" }
        val PublicFixture2 = fixture<String, String>("PublicFixture2", PrivateFixture2) { "Public Fixture 2 based on $it" }
        val TestDurationFixture = fixture<Duration>("TestDuration") { Duration.ofDays(1) }
    }

    @Suppress("unused")
    object TestFixtureObject1 : FixtureContainer {
        val TestDurationFixture1 = fixture<Duration>("TestDuration") { Duration.ofDays(1) }
    }

    @Suppress("unused")
    object FixtureContainerWithDuplicatePropertyName : FixtureContainer {
        val DuplicateNameFixture = fixture<String>("FirstKey") { "First Value" }
    }

    @Suppress("unused")
    object FixtureContainerWithDuplicateProperty1 : FixtureContainer {
        val DuplicateNameFixture = fixture<String>("SecondKey") { "Second Value" }
    }
}
