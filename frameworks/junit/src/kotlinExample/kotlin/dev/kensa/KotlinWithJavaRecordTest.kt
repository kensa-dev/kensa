package dev.kensa

import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

class KotlinWithJavaRecordTest : KotlinExampleTest(), WithHamkrest {
    @RenderedValue
    private val myJavaRecord = MyJavaRecord("MyRecordValue")

    @Test
    fun passingTest() {
        given(aJavaRecordWithValue(myJavaRecord.value))
    }

    fun aJavaRecordWithValue(value: String): Action<GivensContext> {
        return Action { }
    }
}