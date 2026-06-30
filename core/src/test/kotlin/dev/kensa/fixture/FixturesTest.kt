package dev.kensa.fixture

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate

class FixturesTest {

    @Test
    fun `should create primary fixture with lazy initialization`() {
        var factoryCallCount = 0
        val dateFixture = fixture<LocalDate>("date") { factoryCallCount++; LocalDate.of(2023, 1, 1) }

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
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture) { date -> factoryCallCount++; date.dayOfWeek }

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
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture) { date -> date.dayOfWeek }
        val dayPlusOneFixture = fixture<DayOfWeek, DayOfWeek>("dayPlusOne", dayFixture) { day -> day.plus(1) }

        val fixtures = Fixtures()

        fixtures[dayFixture] shouldBe SUNDAY

        fixtures[dayPlusOneFixture] shouldBe MONDAY
    }

    @Test
    fun `values only contains accessed fixtures`() {
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture) { date -> date.dayOfWeek }

        val fixtures = Fixtures()
        fixtures[dateFixture]

        fixtures.values().map { it.name } shouldBe listOf("date")
    }

    @Test
    fun `values returns fixtures in parent-before-child order`() {
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture) { date -> date.dayOfWeek }
        val dayPlusOneFixture = fixture<DayOfWeek, DayOfWeek>("dayPlusOne", dayFixture) { day -> day.plus(1) }

        val fixtures = Fixtures()
        fixtures[dayPlusOneFixture]

        fixtures.values().map { it.name } shouldBe listOf("date", "day", "dayPlusOne")
    }

    @Test
    fun `specs returns empty parents for primary fixture`() {
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }

        val fixtures = Fixtures()
        fixtures[dateFixture]

        fixtures.specs() shouldBe listOf(FixtureSpec("date", emptyList()))
    }

    @Test
    fun `specs returns parent keys for secondary fixture`() {
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture) { date -> date.dayOfWeek }

        val fixtures = Fixtures()
        fixtures[dayFixture]

        fixtures.specs() shouldBe listOf(
            FixtureSpec("date", emptyList()),
            FixtureSpec("day", listOf("date"))
        )
    }

    @Test
    fun `specs returns parent keys for secondary fixture with two parents`() {
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek>("day") { SUNDAY }
        val combinedFixture = fixture<String, LocalDate, DayOfWeek>("combined", dateFixture, dayFixture) { date, day -> "$date-$day" }

        val fixtures = Fixtures()
        fixtures[combinedFixture]

        fixtures.specs() shouldBe listOf(
            FixtureSpec("date", emptyList()),
            FixtureSpec("day", emptyList()),
            FixtureSpec("combined", listOf("date", "day"))
        )
    }

    @Test
    fun `specs only contains accessed fixtures`() {
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture) { date -> date.dayOfWeek }

        val fixtures = Fixtures()
        fixtures[dateFixture]

        fixtures.specs() shouldBe listOf(FixtureSpec("date", emptyList()))
    }

    @Test
    fun `seed short-circuits the factory and is reflected in specs`() {
        val greeting = parameterFixture("greeting", from = "userName") { name: String -> "Hello, $name" }

        val fixtures = Fixtures()
        fixtures.seed(greeting, "Hello, Alice")

        fixtures[greeting] shouldBe "Hello, Alice"
        fixtures.specs() shouldBe listOf(FixtureSpec("greeting", emptyList()))
    }

    @Test
    fun `factory fixtures memoize distinct values per (key, args) identity`() {
        var counter = 0
        fun make(arg: String) = factoryFixture("MyFixture", arg) { "$arg-${counter++}" }

        val fixtures = Fixtures()

        val a1 = fixtures[make("a")]
        val a2 = fixtures[make("a")]
        val b1 = fixtures[make("b")]

        a1 shouldBe "a-0"
        a2 shouldBe a1
        b1 shouldBe "b-1"
        b1 shouldNotBe a1

        fixtures.values().map { it.name } shouldBe listOf("MyFixture(a)", "MyFixture(b)")
    }

    @Test
    fun `factory fixture with no identity args stores under the bare key`() {
        val f = factoryFixture("Bare") { "value" }

        f.key shouldBe "Bare"
    }

    @Test
    fun `createParameterFixture builds a guarded parameter fixture from the Java crossover`() {
        val greeting = createParameterFixture<String, String>("greeting", "userName") { name -> "Hello, $name" }

        greeting.key shouldBe "greeting"
        greeting.from shouldBe "userName"
        greeting.deriveFrom("alice") shouldBe "Hello, alice"

        shouldThrow<IllegalStateException> {
            Fixtures()[greeting]
        }.shouldHaveMessage("Parameter fixture 'greeting' must be seeded from test parameter 'userName' — it cannot be resolved without a parameterised invocation.")
    }

    @Test
    fun `resolving an un-seeded parameter fixture throws a clear error`() {
        val greeting = parameterFixture("greeting", from = "userName") { name: String -> "Hello, $name" }

        val fixtures = Fixtures()

        shouldThrow<IllegalStateException> {
            fixtures[greeting]
        }.shouldHaveMessage("Parameter fixture 'greeting' must be seeded from test parameter 'userName' — it cannot be resolved without a parameterised invocation.")
    }

    @Test
    fun `highlighted parameter fixture value appears in highlightedValues once seeded`() {
        val greeting = parameterFixture("greeting", from = "userName", highlighted = true) { name: String -> "Hello, $name" }

        val fixtures = Fixtures()
        fixtures.seed(greeting, "Hello, Alice")

        val values = fixtures.highlightedValues()
        values.map { it.name } shouldBe listOf("greeting")
        values.map { it.value } shouldBe listOf("Hello, Alice")
    }

    @Test
    fun `highlighted primary fixture value appears in highlightedValues once accessed`() {
        val highlighted = fixture<LocalDate>("date", highlighted = true) { LocalDate.of(2023, 1, 1) }
        val notHighlighted = fixture<DayOfWeek>("day") { SUNDAY }

        val fixtures = Fixtures()

        fixtures[highlighted]
        fixtures[notHighlighted]

        val values = fixtures.highlightedValues()
        values.map { it.name } shouldBe listOf("date")
        values.map { it.value } shouldBe listOf(LocalDate.of(2023, 1, 1))
    }

    @Test
    fun `highlighted secondary fixture value appears in highlightedValues once accessed`() {
        val dateFixture = fixture<LocalDate>("date") { LocalDate.of(2023, 1, 1) }
        val dayFixture = fixture<DayOfWeek, LocalDate>("day", dateFixture, highlighted = true) { date -> date.dayOfWeek }

        val fixtures = Fixtures()
        fixtures[dayFixture]

        val values = fixtures.highlightedValues()
        values.map { it.name } shouldBe listOf("day")
    }

    @Test
    fun `unaccessed highlighted fixture does not appear in highlightedValues`() {
        val highlighted = fixture<LocalDate>("date", highlighted = true) { LocalDate.of(2023, 1, 1) }

        val fixtures = Fixtures()

        fixtures.highlightedValues() shouldBe emptySet()

        fixtures[highlighted]

        fixtures.highlightedValues().map { it.name } shouldBe listOf("date")
    }
}
