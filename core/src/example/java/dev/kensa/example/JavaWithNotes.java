package dev.kensa.example;

public class JavaWithNotes {

    public void notesTest() {
        /// Note before Given
        given(() -> {
                } /// Note after closing brace
        );

        /// Note before When
        when(() -> {
        });

        /// Note before Then
        then(() -> {
            thing(); /// Note after closing bracket
        });
    }

    public void ignoredNotesTest() {
        /// Ignored (must immediately precede BDD keyword)

        given(() -> { /// Ignored
        /// Ignored
        });

        /// Ignored

        /// Note before When
        when(() -> {
        });
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