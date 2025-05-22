package dev.kensa.fixture;

import java.time.Duration;

/**
 * A Java class implementing FixtureContainer for testing purposes.
 */
public class JavaFixtureClass implements FixtureContainer {

    // Static fixture definition
    public static final Fixture<Duration> DURATION_FIXTURE =
        FixtureKt.createFixture("JavaDurationFixture", () -> Duration.ofDays(1));
}
