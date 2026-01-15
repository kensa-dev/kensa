package dev.kensa.example;

import dev.kensa.GivensBuilder;
import dev.kensa.StateExtractor;
import dev.kensa.fixture.FixtureContainer;
import dev.kensa.fixture.FixtureRegistry;
import dev.kensa.fixture.PrimaryFixture;
import dev.kensa.fixture.SecondaryFixture;
import dev.kensa.hamcrest.WithHamcrest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static dev.kensa.example.JavaTestFixtures.CHILD_STRING_FIXTURE;
import static dev.kensa.example.JavaTestFixtures.STRING_FIXTURE;
import static dev.kensa.fixture.FixtureKt.createFixture;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;

public class JavaWithFixturesTest extends JavaExampleTest implements WithHamcrest {

    static {
        FixtureRegistry.registerFixtures(JavaTestFixtures.class, MoreJavaTestFixtures.class);
    }

    @Test
    void test1() {
        given(somePrerequisites());

        then(theStringFixture(), is(fixtures(STRING_FIXTURE)));
        then(theBooleanFixture(), is(fixtures(MoreJavaTestFixtures.BOOLEAN_FIXTURE)));
    }

    @Test
    void test2() {
        given(somePrerequisites());

        then(theStringFixture(), is(fixtures(STRING_FIXTURE)));
        then(theFixture(fixtures(CHILD_STRING_FIXTURE)), startsWith(fixtures(STRING_FIXTURE)));
    }

    @Test
    void test3() {
        then(theFixture(fixtures(JavaTestFixtures.PUBLIC_FIXTURE)), is(222));
    }

    private GivensBuilder somePrerequisites() {
        return (givens) -> givens.put("foo", fixtures(STRING_FIXTURE));
    }

    private StateExtractor<String> theStringFixture() {
        return interactions -> fixtures(STRING_FIXTURE);
    }

    private StateExtractor<Boolean> theBooleanFixture() {
        return interactions -> fixtures(MoreJavaTestFixtures.BOOLEAN_FIXTURE);
    }

    private <T> StateExtractor<T> theFixture(T value) {
        return interactions -> value;
    }
}

class JavaTestFixtures implements FixtureContainer {
    private static final List<String> STRING_FIXTURES = new ArrayList<>(List.of("parent1", "parent2"));
    private static final List<String> CHILD_STRING_FIXTURES = new ArrayList<>(List.of("child1", "childe2"));

    public static final PrimaryFixture<String> STRING_FIXTURE = createFixture("JavaStringFixture", STRING_FIXTURES::removeFirst);
    public static final SecondaryFixture<String> CHILD_STRING_FIXTURE = createFixture("JavaChildStringFixture", STRING_FIXTURE, parent -> parent + "_" + CHILD_STRING_FIXTURES.removeFirst());

    private static final PrimaryFixture<Integer> PRIVATE_FIXTURE = createFixture("JavaPrivateFixture", () -> 111);
    public static final SecondaryFixture<Integer> PUBLIC_FIXTURE = createFixture("JavaPublicFixture", PRIVATE_FIXTURE, parent -> parent + 111);
}

class MoreJavaTestFixtures implements FixtureContainer {
    public static final PrimaryFixture<Boolean> BOOLEAN_FIXTURE = createFixture("JavaBooleanFixture", () -> true);
}
