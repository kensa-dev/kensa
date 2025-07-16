package dev.kensa.fixture

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

class FixturesTest {

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
    fun `secondary fixtures can be children of other secondary fixtures`() {
        val dateFixture = fixture<LocalDate>("date", { LocalDate.of(2023, 1, 1) })
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture, { date -> date.dayOfWeek })
        val dayPlusOneFixture = fixture<DayOfWeek, DayOfWeek>("dayPlusOne", dayFixture, { day -> day.plus(1) })

        val fixtures = Fixtures()

        fixtures[dayFixture] shouldBe SUNDAY

        fixtures[dayPlusOneFixture] shouldBe MONDAY
    }
}
