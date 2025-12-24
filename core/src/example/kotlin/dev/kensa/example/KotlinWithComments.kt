package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat

// Line comments before the class declaration should be ignored
/// Kensa Note before the class declaration should be ignored
/* Delimited comments before the class declaration should be ignored */
/*+ Kensa hint before the class declaration should be ignored */

class KotlinWithComments {

    // Line comments before the function should be ignored
    /// Kensa Note before the function should be ignored
    /* Delimited comments before the function should be ignored */
    /*+ Kensa hint before the function should be ignored */

    fun kensaNotesTest(
        first: String?, /// Kensa note after first test param should be ignored
        second: Int?    /// Kensa note after second test param should be ignored
    ) {
        /// Kensa note at start of block
        assertThat(true).isEqualTo(true)

        /// Kensa note between statements

        assertThat(                    /// Kensa note before method call param should be ignored
            false                      /// Kensa note after method call param should be ignored
        )                              /// Kensa note after method call
            .                          /// Kensa note after dereference operator should be ignored
            isEqualTo(false) /// Kensa note at end of statement line

        listOf(1,2).forEach {
            /// Kensa note at start of for loop
            assertThat(it).isEqualTo( /// Kensa note before arg name should be ignored
                expected              /// Kensa note after arg name should be ignored
                =                     /// Kensa note at start of arg value expression should be ignored
                    it                /// Kensa note after first operand should be ignored
                            +         /// Kensa note after operator should be ignored
                            0         /// Kensa note after second operand should be ignored
                /// Kensa note on own line after final method call param should be ignored
            )
            /// Kensa note at end of for loop should be ignored
        }

        /// Kensa note after final statement should be ignored

    }

    fun ignoredStandardLineCommentsTest(
        first: String?, // Line comment after first test param should be ignored
        second: Int?    // Line comment after second test param should be ignored
    ) {
        // Line comment at start of block
        assertThat(true).isEqualTo(true)

        // Line comment between statements

        assertThat(                    // Line comment before method call param
            false                      // Line comment after method call param
        )                              // Line comment after method call
            .                          // Line comment after dereference operator
            isEqualTo(false) // Line comment at end of statement line

        listOf(1,2).forEach {
            // Line comment at start of for loop
            assertThat(it).isEqualTo( // Line comment before arg name
                expected              // Line comment after arg name
                =                     // Line comment at start of arg value expression
                    it                // Line comment after first operand
                            +         // Line comment after operator
                            0         // Line comment after second operand
                // Line comment on own line after final method call param
            )
            // Line comment at end of for loop
        }

        // Line comment after final statement
    }

    // Line comments between functions should be ignored
    /// Kensa Note between functions should be ignored
    /* Delimited comments between functions should be ignored */
    /*+ Kensa hint between functions should be ignored */

    fun kensaHintsTest(
        first: String?, /*+ Delimited comment after first test param should be ignored */
        second: Int?    /*+ Delimited comment after second test param should be ignored */
    ) {
        /*+ Delimited comment at start of block */
        assertThat(true).isEqualTo(true)

        /*+ Delimited comment between statements */

        assertThat(                    /*+ Delimited comment before method call param */
            false                      /*+ Delimited comment after method call param */
        )                              /*+ Delimited comment after method call */
            .                          /*+ Delimited comment after dereference operator */
            isEqualTo(false) /*+ Delimited comment at end of statement line */

        /**
         * Multi-line
         * comment
         */
        listOf(1,2).forEach {
            /*+ Delimited comment at start of for loop */
            /*+ Delimited comment at start of line */assertThat/*+ Delimited comment mid-line */(it).isEqualTo( /*+ Delimited comment before arg name */
                expected              /*+ Delimited comment after arg name */
                =                     /*+ Delimited comment at start of arg value expression */
                    it                /*+ Delimited comment after first operand */
                            +         /*+ Delimited comment after operator */
                            0         /*+ Delimited comment after second operand */
                /*+ Delimited comment on own line after final method call param */
            )
            /*+ Delimited comment at end of for loop */
        }

        /*+ Delimited comment after final statement */
    }

    fun ignoredStandardDelimitedCommentsTest(
        first: String?, /* Delimited comment after first test param */
        second: Int?    /* Delimited comment after second test param */
    ) {
        /* Delimited comment at start of block */
        assertThat(true).isEqualTo(true)

        /* Delimited comment between statements */

        assertThat(                    /* Delimited comment before method call param */
            false                      /* Delimited comment after method call param */
        )                              /* Delimited comment after method call */
            .                          /* Delimited comment after dereference operator */
            isEqualTo(false) /* Delimited comment at end of statement line */

        /*
         * Multi-line
         * comment
         */
        listOf(1,2).forEach {
            /* Delimited comment at start of for loop */
            /* Delimited comment at start of line */assertThat/* Delimited comment mid-line */(it).isEqualTo( /* Delimited comment before arg name */
                expected              /* Delimited comment after arg name */
                =                     /* Delimited comment at start of arg value expression */
                    it                /* Delimited comment after first operand */
                            +         /* Delimited comment after operator */
                            0         /* Delimited comment after second operand */
                /* Delimited comment on own line after final method call param */
            )
            /* Delimited comment at end of for loop */
        }

        /* Delimited comment after final statement */
    }

    // Line comments after the function should be ignored
    /// Kensa Note after the function should be ignored
    /* Delimited comments after the function should be ignored */
    /*+ Kensa hint after the function should be ignored */
}

// Line comments after the class declaration should be ignored
/// Kensa Note after the class declaration should be ignored
/* Delimited comments after the class declaration should be ignored */
/*+ Kensa hint after the class declaration should be ignored */
