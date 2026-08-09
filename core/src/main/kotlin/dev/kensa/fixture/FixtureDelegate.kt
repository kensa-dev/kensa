package dev.kensa.fixture

import dev.kensa.KensaException
import dev.kensa.context.TestContext
import dev.kensa.context.TestContextHolder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Property delegate that resolves a fixture's value from the current thread's active test context on every
 * access. Lets long-lived objects (parameterised test data, scenario holders) expose per-invocation fixture
 * values as plain properties without holding a test context themselves:
 *
 * ```
 * data class WholesalerUseCase(private val referenceFx: Fixture<ProviderOrderReference>) {
 *     @get:RenderedValue
 *     val providerOrderReference: ProviderOrderReference by fixtures(referenceFx)
 * }
 * ```
 *
 * The value is re-read on every access, so the same instance can be shared across parameterised test
 * invocations and parallel threads; each access sees the fixture value seeded for the test running on the
 * accessing thread.
 */
class FixtureDelegate<T>(val fixture: Fixture<T>) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val context: TestContext = TestContextHolder.testContextOrNull()
            ?: throw KensaException(
                "Property [${property.name}] delegates to fixture [${fixture.key}] but was accessed outside an active Kensa test"
            )
        return context.fixtures[fixture]
    }
}

fun <T> fixtures(fixture: Fixture<T>): FixtureDelegate<T> = FixtureDelegate(fixture)
