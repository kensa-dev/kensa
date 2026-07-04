// Snippet source for kensa.dev/docs/api/attachments.md — Kotlin tab
package apidocs

import dev.kensa.attachments.TypedKey
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MySnapshot

val mySnapshotKey = TypedKey<MySnapshot>("my-snapshot")

class SomeTest : KensaTest, WithKotest {

    @Test
    fun `something happens`() {
        whenever {
            val snapshot = capture()
            testContext().attachments.put(mySnapshotKey, snapshot)
        }
        then({ testContext().attachments.getOrNull(mySnapshotKey) }) {
            this shouldBe this
        }
    }

    private fun capture(): MySnapshot = MySnapshot()
}
