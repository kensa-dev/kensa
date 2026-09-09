// Snippet source for kensa.dev/docs/api/setup-steps.md
package apidocs

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.ActionBlockBuilder.Companion.buildActions
import dev.kensa.GivensBlockBuilder.Companion.buildGivens
import dev.kensa.SetupScope
import dev.kensa.StateCollector
import dev.kensa.VerificationBlockBuilder.Companion.verify
import dev.kensa.and
import dev.kensa.hamkrest.HamkrestSetupStep
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.kotest.kotestSetupStep
import dev.kensa.setupActions
import dev.kensa.setupStep
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class SetupStepsSnippets : KensaTest, WithHamkrest {

    private lateinit var account: String
    private lateinit var reference: String

    @Volatile
    private var settled: String? = null

    @Test
    fun `account is opened`() {
        given(anAccountIsOpened())

        then(theAccount(), equalTo("opened-account"))
    }

    @Test
    fun `account is opened and funded as a chain`() {
        given(anAccountIsOpened().and(theAccountIsFunded()))

        then(theAccount(), equalTo("opened-account"))
        then(theReference(), equalTo("funded-account"))
    }

    @Test
    fun `account is opened then separately funded`() {
        given(anAccountIsOpened())
        and(theAccountIsFunded())

        then(theAccount(), equalTo("opened-account"))
        then(theReference(), equalTo("funded-account"))
    }

    @Test
    fun `balance settles before the test continues`() {
        given(theBalanceSettles())

        then(theAccount(), equalTo("settled-account"))
    }

    @Test
    fun `explicit setup override behaves like the default triple`() {
        given(anAccountIsOpenedViaExplicitSetup())

        then(theAccount(), equalTo("opened-account"))
    }

    @Test
    fun `setupActions builds a bare action sequence`() {
        given(anAccountIsOpenedAndFundedViaSetupActions())

        then(theAccount(), equalTo("opened-account"))
        then(theReference(), equalTo("funded-account"))
    }

    @Test
    fun `setupStep builds anything richer than a bare action sequence`() {
        given(anAccountIsOpenedViaSetupStep())

        then(theAccount(), equalTo("opened-account"))
    }

    private fun theAccount() = StateCollector { account }
    private fun theReference() = StateCollector { reference }

    private fun anAccountIsOpenedAndFundedViaSetupActions() = setupActions(
        { account = "opened-account" },
        { reference = "funded-account" }
    )

    private fun anAccountIsOpenedViaSetupStep() = setupStep {
        given { account = "opened-account" }
        verify { check(account == "opened-account") }
    }

    private fun anAccountIsOpened() = object : HamkrestSetupStep {
        override fun givens() = buildGivens { }

        override fun actions() = buildActions {
            add { account = "opened-account" }
        }

        override fun verify() = verify {
            assertThat(account, equalTo("opened-account"))
        }
    }

    private fun anAccountIsOpenedViaExplicitSetup() = object : HamkrestSetupStep {
        override fun givens() = buildGivens { }

        override fun actions() = buildActions {
            add { account = "opened-account" }
        }

        override fun verify() = verify {
            assertThat(account, equalTo("opened-account"))
        }

        override fun setup(scope: SetupScope) {
            scope.given { givens().buildWith(it).executeWith(it) }
            scope.action { actions().buildWith(it).executeWith(it) }
            scope.verify { verify().verifyWith(it) }
        }
    }

    private fun theAccountIsFunded() = object : HamkrestSetupStep {
        override fun givens() = buildGivens { }

        override fun actions() = buildActions {
            add { reference = "funded-account" }
        }

        override fun verify() = verify {
            assertThat(reference, equalTo("funded-account"))
        }
    }

    private fun theBalanceSettles() = object : HamkrestSetupStep {
        override fun setup(scope: SetupScope) = with(scope) {
            action {
                Thread {
                    Thread.sleep(50)
                    settled = "settled"
                }.apply { isDaemon = true }.start()
            }

            verifyEventually(5.seconds) {
                if (settled == null) throw AssertionError("balance not settled yet")
            }

            val seen = collect(StateCollector { settled!! })

            action { account = seen.replace("settled", "settled-account") }
        }
    }
}

class SetupStepsKotestSnippets : KensaTest, WithKotest {

    private lateinit var account: String

    @Test
    fun `kotestSetupStep puts kotest matchers in scope`() {
        given(anAccountIsOpenedViaKotestSetupStep())

        then(StateCollector { account }) { this shouldBe "opened-account" }
    }

    private fun anAccountIsOpenedViaKotestSetupStep() = kotestSetupStep {
        action { account = "opened-account" }
        then(StateCollector { account }) { this shouldBe "opened-account" }
    }
}
