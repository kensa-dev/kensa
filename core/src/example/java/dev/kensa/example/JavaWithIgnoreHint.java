package dev.kensa.example;

public class JavaWithIgnoreHint {

    public void withIgnoredStatement() {
        given(() -> {
        });

        /*+ Ignore */
        somethingNoisy();

        when(() -> {
        });
    }

    public void withIgnoredLineInBlock() {
        then(() -> {
            /*+ Ignore */
            somethingNoisy();
            thing();
        });
    }

    private void somethingNoisy() {
    }

    private void thing() {
    }

    private void given(Runnable block) {
        block.run();
    }

    private void when(Runnable block) {
        block.run();
    }

    private void then(Runnable block) {
        block.run();
    }
}
