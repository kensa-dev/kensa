package dev.kensa.example;

import dev.kensa.RenderedValue;

public class JavaTestInstance {
    @RenderedValue
    private TestScenario scenario = new TestScenario();
    private TestScenario nullScenario = null;

    static class TestScenario {
        private String value = "FooBoo!!";

        public String aValue() {
            String value = this.value;
            this.value = "Meh!";
            return value;
        }

        public String aNullValue() {
            return null;
        }
    }
}
