package dev.kensa.fixture;

import java.time.Duration;

import static dev.kensa.fixture.FixtureKt.createFixture;

/**
 * A Java class implementing FixtureContainer for testing purposes.
 */
public class JavaFixtureClass implements FixtureContainer {

    public static final Fixture<Duration> DURATION_FIXTURE = FixtureKt.createFixture("JavaDurationFixture", () -> Duration.ofDays(1));

    private static final PrimaryFixture<Integer> PRIVATE_FIXTURE = createFixture("JavaPrivateFixture", () -> 111);
    public static final SecondaryFixture<Integer> PUBLIC_FIXTURE = createFixture("JavaPublicFixture", PRIVATE_FIXTURE, parent -> parent + 111);

}
