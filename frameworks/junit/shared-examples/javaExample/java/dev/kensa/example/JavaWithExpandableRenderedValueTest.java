package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.ExpandableRenderedValue;
import dev.kensa.GivensContext;
import dev.kensa.RenderedValueStyle;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JavaWithExpandableRenderedValueTest extends JavaExampleTest {

    @ExpandableRenderedValue
    private final List<String> theRecordedLifecycle = List.of("Acknowledged", "Committed", "Dispatched");

    @ExpandableRenderedValue(renderAs = RenderedValueStyle.Tabular, headers = {"Currency", "Rate"})
    private final List<List<String>> theStoredRates = List.of(List.of("GBPUSD", "1.2710"), List.of("EURUSD", "1.0885"));

    @Test
    void rendersAnExpandableValueFromAField() {
        given(aDispatchedShipment());

        whenever(theCourierReports(theRecordedLifecycle));
    }

    @Test
    void rendersATableFromAField() {
        given(aDispatchedShipment());

        whenever(theCourierReports(theStoredRates));
    }

    private Action<ActionContext> theCourierReports(Object report) {
        return context -> {
        };
    }

    private Action<GivensContext> aDispatchedShipment() {
        return context -> {
        };
    }
}
