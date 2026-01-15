package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.GivensContext;
import dev.kensa.StateCollector;
import dev.kensa.hamcrest.WithHamcrest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class JavaWithNotesTest extends JavaExampleTest implements WithHamcrest {

    @Test
    public void passingTest() {
        /// These are notes
        given(something());

        /// These are also notes
        /// on two lines
        whenever(somethingHappens());

        /// Notes on then
        then(
            things(), /// End of line notes
            equalTo(true) /// More end of line notes
        );
    }

    private Action<GivensContext> something() {
        return (ctx) -> {};
    }

    private Action<ActionContext> somethingHappens() {
        return (ctx) -> {};
    }

    private StateCollector<Boolean> things() {
        return (ctx) -> true;
    }
}