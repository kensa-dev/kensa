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
            val TestDateFixture = fixture<LocalDate>(key = "TestDate", factory = { LocalDate.now() })
            val TestDayFixture = fixture<DayOfWeek, LocalDate>(
                key = "TestDay",
                parentFixture = TestDateFixture,
                factory = { date -> date.dayOfWeek }
            )
        }
    }

    @Suppress("unused")
    object TestFixtureObject : FixtureContainer {
        val TestDurationFixture = fixture<Duration>(key = "TestDuration", factory = { Duration.ofDays(1) })
    }

    @Suppress("unused")
    object TestFixtureObject1 : FixtureContainer {
        val TestDurationFixture1 = fixture<Duration>(key = "TestDuration", factory = { Duration.ofDays(1) })
    }

    @Suppress("unused")
    object FixtureContainerWithDuplicatePropertyName : FixtureContainer {
        val DuplicateNameFixture = fixture<String>(key = "FirstKey", factory = { "First Value" })
    }

    @Suppress("unused")
    object FixtureContainerWithDuplicateProperty1 : FixtureContainer {
        val DuplicateNameFixture = fixture<String>(key = "SecondKey", factory = { "Second Value" })
    }
}
