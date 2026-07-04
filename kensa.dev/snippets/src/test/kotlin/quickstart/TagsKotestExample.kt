// Snippet source for kensa.dev/docs/tags.md Kotest example
package quickstart

import dev.kensa.kotest.KensaTest
import io.kotest.core.annotation.Tags

@Tags("Checkout", "Smoke")
class TagsKotestExample : KensaTest() {

    @Test
    fun canCompleteCheckout() {
        // ...
    }
}
