package dev.kensa

import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test

class KotlinWithJavaRecordTest : KensaTest, WithHamkrest {
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