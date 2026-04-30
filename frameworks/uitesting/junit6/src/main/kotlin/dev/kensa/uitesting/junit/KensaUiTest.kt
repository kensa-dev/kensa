package dev.kensa.uitesting.junit

import dev.kensa.*
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.Fixtures
import dev.kensa.junit.KensaExtension
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.uitesting.BrowserDriver
import dev.kensa.uitesting.UserStub
import dev.kensa.uitesting.WithScreenshots
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Base class for UI tests. Extend this class and implement [createDriver] and [createUser].
 *
 * The underlying [BrowserDriver] is created fresh per test and torn down automatically.
 * Test authors interact only with [theUser] and the Kensa BDD DSL ([given]/[whenever]).
 *
 * Example:
 * ```kotlin
 * class LoginTest : KensaUiTest<TheUser>() {
 *     override fun createDriver() = SeleniumBrowserDriver(ChromeDriver())
 *     override fun createUser(driver: BrowserDriver) = TheUser(driver)
 *
 *     @Test fun `user can log in`() {
 *         given { theUser.navigatesToLoginPage() }
 *         whenever { theUser.submitsLoginForm("alice") }
 *     }
 * }
 * ```
 */
@ExtendWith(KensaExtension::class, KensaUiExtension::class)
abstract class KensaUiTest<U : UserStub<*>> : WithScreenshots, WithFixturesAndOutputs {

    /** Create the browser driver for this test. Called once per test method. */
    abstract fun createDriver(): BrowserDriver

    /** Create the user stub backed by [driver]. Called once per test method. */
    abstract fun createUser(driver: BrowserDriver): U

    /** The user workflow object. Available from within test methods. */
    lateinit var theUser: U

    /** Internal: the driver held by the extension for auto-capture and teardown. */
    internal lateinit var _driver: BrowserDriver

    internal fun installDriverAndUser() {
        _driver = createDriver()
        theUser = createUser(_driver)
    }

    internal fun quitDriver() {
        if (::_driver.isInitialized) _driver.quit()
    }

    // ── Kensa BDD DSL ─────────────────────────────────────────────────────────

    fun given(action: Action<GivensContext>) = testContext().given(action)
    fun given(steps: SetupSteps) = testContext().given(steps)
    fun given(step: SetupStep) = given(SetupSteps(step))
    fun and(action: Action<GivensContext>) = given(action)
    fun and(steps: SetupSteps) = given(steps)
    fun and(step: SetupStep) = given(SetupSteps(step))
    fun `when`(action: Action<ActionContext>) = whenever(action)
    fun whenever(action: Action<ActionContext>) = testContext().whenever(action)

    override val fixturesAndOutputs: FixturesAndOutputs get() = testContext().fixturesAndOutputs
    override val fixtures: Fixtures get() = testContext().fixtures
    override val outputs: CapturedOutputs get() = testContext().outputs
}
