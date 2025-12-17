package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat

// Line comments before the class declaration should be ignored
/// Line comments before the class declaration should be ignored
/* Delimited comments before the class declaration should be ignored */
/** Delimited comments before the class declaration should be ignored */

class KotlinWithComments {

    // Line comments before the function should be ignored
    /// Line comments before the function should be ignored
    /* Delimited comments before the function should be ignored */
    /** Delimited comments before the function should be ignored */

    fun kensaStyleLineCommentsTest(
        first: String?, /// Line comment after first test param should be ignored
        second: Int?    /// Line comment after second test param should be ignored
    ) {
        /// Line comment at start of block
        assertThat(true).isEqualTo(true)

        /// Line comment between statements

        assertThat(                    /// Line comment before method call param
            false                      /// Line comment after method call param
        )                              /// Line comment after method call
            .                          /// Line comment after dereference operator
            isEqualTo(false) /// Line comment at end of statement line

        listOf(1,2).forEach {
            /// Line comment at start of for loop
            assertThat(it).isEqualTo( /// Line comment before arg name
                expected              /// Line comment after arg name
                =                     /// Line comment at start of arg value expression
                    it                /// Line comment after first operand
                            +         /// Line comment after operator
                            0         /// Line comment after second operand
                /// Line comment on own line after final method call param
            )
            /// Line comment at end of for loop
        }

        /// Line comment after final statement
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
    /// Line comments between functions should be ignored
    /* Delimited comments between functions should be ignored */
    /** Delimited comments between functions should be ignored */

    fun kensaStyleDelimitedCommentsTest(
        first: String?, /** Delimited comment after first test param */
        second: Int?    /** Delimited comment after second test param */
    ) {
        /** Delimited comment at start of block */
        assertThat(true).isEqualTo(true)

        /** Delimited comment between statements */

        assertThat(                    /** Delimited comment before method call param */
            false                      /** Delimited comment after method call param */
        )                              /** Delimited comment after method call */
            .                          /** Delimited comment after dereference operator */
            isEqualTo(false) /** Delimited comment at end of statement line */

        /**
         * Multi-line
         * comment
         */
        listOf(1,2).forEach {
            /** Delimited comment at start of for loop */
            /** Delimited comment at start of line */assertThat/** Delimited comment mid-line */(it).isEqualTo( /** Delimited comment before arg name */
                expected              /** Delimited comment after arg name */
                =                     /** Delimited comment at start of arg value expression */
                    it                /** Delimited comment after first operand */
                            +         /** Delimited comment after operator */
                            0         /** Delimited comment after second operand */
                /** Delimited comment on own line after final method call param */
            )
            /** Delimited comment at end of for loop */
        }

        /** Delimited comment after final statement */
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
    /// Line comments after the function should be ignored
    /* Delimited comments after the function should be ignored */
    /** Delimited comments after the function should be ignored */
}

// Line comments after the class declaration should be ignored
/// Line comments after the class declaration should be ignored
/* Delimited comments after the class declaration should be ignored */
/** Delimited comments after the class declaration should be ignored */
