package dev.kensa.fixture

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Duration

/**
 * Example usage of the FixtureMap class.
 */
class FixturesExample {
    companion object : FixtureContainer {
        val MessageDateFixture = fixture<LocalDate>(key = "MessageDateProperty", factory = { LocalDate.now() })
        val MessageDayFixture = fixture<DayOfWeek, LocalDate>(
            key = "MessageDayProperty",
            parentFixture = MessageDateFixture,
            factory = { date -> date.dayOfWeek }
        )
        val DurationFixture = fixture<Duration>(key = "DurationMsProperty", factory = { Duration.ofDays(1) })
    }

    fun demonstrateFixtureMap() {
        val fixtures = Fixtures()

        // Get the message date (lazy initialization)
        val messageDate: LocalDate = fixtures[MessageDateFixture]
        println("Message date: $messageDate")

        // Get the message day (derived from message date)
        val messageDay: DayOfWeek = fixtures[MessageDayFixture]
        println("Message day: $messageDay")

        // Get the duration
        val duration: Duration = fixtures[DurationFixture]
        println("Duration: $duration")

        // Set a new value for the parent fixture
        fixtures[MessageDateFixture] = LocalDate.of(2023, 1, 1)

        // Get the updated message date
        val updatedMessageDate: LocalDate = fixtures[MessageDateFixture]
        println("Updated message date: $updatedMessageDate")

        // Get the updated message day (derived from the updated message date)
        val updatedMessageDay: DayOfWeek = fixtures[MessageDayFixture]
        println("Updated message day: $updatedMessageDay")

        // Attempting to set a value for a child fixture would throw an exception
        // fixtureMap[MessageDayFixture] = DayOfWeek.MONDAY // This would throw an IllegalArgumentException
    }
}
