// Snippet source for kensa.dev/docs/quickstart/kotest-quickstart.md — listener registration
package io.kotest.provided

import dev.kensa.kotest.KensaKotestListener
import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override val extensions = listOf(KensaKotestListener())
}
