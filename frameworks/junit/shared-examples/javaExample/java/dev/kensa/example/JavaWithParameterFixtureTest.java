package dev.kensa.example;

import dev.kensa.StateCollector;
import dev.kensa.fixture.FixtureContainer;
import dev.kensa.fixture.FixtureRegistry;
import dev.kensa.fixture.ParameterFixture;
import dev.kensa.hamcrest.WithHamcrest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.kensa.example.JavaParameterFixtures.GREETING;
import static dev.kensa.fixture.FixtureKt.createParameterFixture;
import static org.hamcrest.CoreMatchers.is;

public class JavaWithParameterFixtureTest extends JavaExampleTest implements WithHamcrest {

    static {
        FixtureRegistry.registerFixtures(JavaParameterFixtures.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob"})
    void greetsTheUser(String userName) {
        then(theGreeting(), is(fixtures(GREETING)));
    }

    private StateCollector<String> theGreeting() {
        return context -> fixtures(GREETING);
    }
}

class JavaParameterFixtures implements FixtureContainer {
    public static final ParameterFixture<String> GREETING = createParameterFixture("greeting", "userName", (String name) -> "Hello, " + name);
}
