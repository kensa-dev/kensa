package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.RenderedValue
import dev.kensa.SetupScope
import dev.kensa.StateCollector
import dev.kensa.hamkrest.HamkrestSetupStep
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class KotlinWithSetupScopeTest : KotlinExampleTest(), WithHamkrest {

    private lateinit var aValue: String

    @Volatile
    private var settled: String? = null

    @field:RenderedValue
    private val expectedValue = "aStringValue"

    @Test
    fun sequencedSetupStep() {
        given(aSequencedTask())

        then(theValue(), equalTo(expectedValue))
    }

    private fun theValue() = StateCollector { aValue }

    private fun aSequencedTask() = object : HamkrestSetupStep {
        override fun setup(scope: SetupScope) = with(scope) {
            action {
                Thread {
                    Thread.sleep(50)
                    settled = "settled"
                }.apply { isDaemon = true }.start()
            }

            verifyEventually(5.seconds) {
                if (settled == null) throw AssertionError("not settled yet")
            }

            val seen = collect(StateCollector { settled!! })

            action { aValue = seen.replace("settled", expectedValue) }
        }
    }
}
