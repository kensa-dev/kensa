package dev.kensa.example

import dev.kensa.Action
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
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