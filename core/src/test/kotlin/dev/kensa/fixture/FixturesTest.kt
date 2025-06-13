package dev.kensa.fixture

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

class FixturesTest {

    @Test
    fun `can check whether fixture has an initialised value`() {
        val stringFixture = fixture<String>("string")
        val fixtures = Fixtures()

        fixtures.hasValue(stringFixture) shouldBe false

        fixtures[stringFixture] = "foo"

        fixtures.hasValue(stringFixture) shouldBe true
    }

    @Test
    fun `should create primary fixture with lazy initialization`() {
        var factoryCallCount = 0
        val dateFixture = fixture<LocalDate>("date", { factoryCallCount++; LocalDate.of(2023, 1, 1) })

        val fixtures = Fixtures()

        factoryCallCount shouldBe 0

        val date: LocalDate = fixtures[dateFixture]

        factoryCallCount shouldBe 1

        date shouldBe LocalDate.of(2023, 1, 1)

        val date1: LocalDate = fixtures[dateFixture]

        factoryCallCount shouldBe 1

        date1 shouldBe LocalDate.of(2023, 1, 1)
    }

    @Test
    fun `should create child fixture derived from primary fixture lazily`() {
        var factoryCallCount = 0
        val dateFixture = fixture<LocalDate>("date", { LocalDate.of(2023, 1, 1) })
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture, { date -> factoryCallCount++; date.dayOfWeek })

        val fixtures = Fixtures()

        factoryCallCount shouldBe 0

        val day: DayOfWeek = fixtures[dayFixture]

        factoryCallCount shouldBe 1

        day shouldBe SUNDAY

        val day1: DayOfWeek = fixtures[dayFixture]

        factoryCallCount shouldBe 1

        day1 shouldBe SUNDAY
    }

    @Test
    fun `should update child fixture when primary fixture is updated - single parent fixture`() {
        val initialDate = LocalDate.of(2023, 1, 1)
        val dateFixture = fixture<LocalDate>("date", { initialDate })
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture, { date -> date.dayOfWeek })

        val fixtures = Fixtures()

        val initialDay: DayOfWeek = fixtures[dayFixture]
        initialDay shouldBe SUNDAY

        fixtures[dateFixture] = LocalDate.of(2023, 1, 2)

        val updatedDay: DayOfWeek = fixtures[dayFixture]
        updatedDay shouldBe MONDAY
    }

    @Test
    fun `should update child fixture when primary fixture is updated - double parent fixture`() {
        val string1 = "String 1"
        val string2 = "String 2"

        val stringFixture1 = fixture<String>("string1", { string1 })
        val stringFixture2 = fixture<String>("string2", { string2 })
        val concatFixture = fixture("concat", stringFixture1, stringFixture2, { s1, s2 -> "$s1::$s2" })

        val fixtures = Fixtures()

        val initialConcatValue = fixtures[concatFixture]
        initialConcatValue shouldBe "String 1::String 2"

        fixtures[stringFixture2] = "String 3"

        val updatedConcatValue = fixtures[concatFixture]
        updatedConcatValue shouldBe "String 1::String 3"
    }

    @Test
    fun `secondary fixtures can be children of other secondary fixtures`() {
        val dateFixture = fixture<LocalDate>("date", { LocalDate.of(2023, 1, 1) })
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture, { date -> date.dayOfWeek })
        val dayPlusOneFixture = fixture<DayOfWeek, DayOfWeek>("dayPlusOne", dayFixture, { day -> day.plus(1) })

        val fixtures = Fixtures()
        val dayValue = fixtures[dayPlusOneFixture]

        dayValue shouldBe MONDAY

        fixtures[dateFixture] = LocalDate.of(2023, 1, 2)

        val dayValue1 = fixtures[dayPlusOneFixture]
        dayValue1 shouldBe DayOfWeek.TUESDAY
    }


}
