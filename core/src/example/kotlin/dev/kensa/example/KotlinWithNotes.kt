package dev.kensa.example

class KotlinWithNotes {

    fun notesTest() {
        /// Note before Given
        given {} /// Note after closing brace

        /// Note before When
        whenever {}

        /// Note before Then
        then {
            thing() /// Note after closing bracket
        }
    }

    fun ignoredNotesTest() {
        /// Ignored (must immediately precede BDD keyword)

        given { /// Ignored
            /// Ignored
        }

        /// Ignored

        /// Note before When
        whenever {}

    }

    private fun thing() {}

    private fun given(block: () -> Unit) {}
    private fun whenever(block: () -> Unit) {}
    private fun then(block: () -> Unit) {}
}