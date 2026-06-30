package dev.kensa.parse

import dev.kensa.FixturesAndOutputs
import dev.kensa.fixture.Fixture
import dev.kensa.fixture.FixtureRegistry
import dev.kensa.outputs.CapturedOutput
import dev.kensa.outputs.CapturedOutputsRegistry
import dev.kensa.util.resolvePath

class FixtureAndOutputAccessor(private val fixturesAndOutputs: FixturesAndOutputs) {

    fun fixtureValue(fixtureName: String, path: String?): Any? {
        val key: Fixture<Any> = FixtureRegistry.lookupFixture(fixtureName)

        return resolvePath(fixturesAndOutputs.fixtures[key], path)
    }

    /** Reads the seeded value of a `@`[dev.kensa.Fixture] factory fixture by its `(key, args)` composite storage key. */
    fun factoryFixtureValue(compositeKey: String): Any? = fixturesAndOutputs.fixtures.valueOf(compositeKey)

    fun outputValueByName(outputName: String, path: String?): Any? {
        val key: CapturedOutput<Any> = CapturedOutputsRegistry.lookupCapturedOutput(outputName)

        return fixturesAndOutputs.outputs.getOrNull(key)?.let {
            resolvePath(it, path)
        }
    }

    fun outputValueByKey(key: String, path: String?): Any? =
        fixturesAndOutputs.outputs.getOrNull<Any>(key)?.let {
            resolvePath(it, path)
        }
}