package dev.kensa.example;

public class JavaWithBlocks {

    public void blocksTest() {
        given(() -> {
        });

        when(() -> {
        });

        then(() -> {
            thing();
        });
    }

    private void thing() {}

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