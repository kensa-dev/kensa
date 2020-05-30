package dev.kensa.parse;

public class JavaTestInstance {
    private TestScenario scenario = new TestScenario();
    private TestScenario nullScenario = null;

    private static class TestScenario {
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
