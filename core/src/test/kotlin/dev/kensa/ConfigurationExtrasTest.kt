package dev.kensa

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

class ConfigurationExtrasTest {

    private data class FakeAddonConfig(var enabled: Boolean = false)
    private data class OtherAddonConfig(var name: String = "")

    @Test
    fun `getExtra returns null when no extra of that type is registered`() {
        Configuration().getExtra(FakeAddonConfig::class).shouldBeNull()
    }

    @Test
    fun `putExtra stores a value retrievable by class`() {
        val configuration = Configuration()
        val extra = FakeAddonConfig(enabled = true)

        configuration.putExtra(FakeAddonConfig::class, extra)

        configuration.getExtra(FakeAddonConfig::class) shouldBeSameInstanceAs extra
    }

    @Test
    fun `putExtra overwrites a previous value of the same type`() {
        val configuration = Configuration()
        configuration.putExtra(FakeAddonConfig::class, FakeAddonConfig(enabled = false))
        val replacement = FakeAddonConfig(enabled = true)

        configuration.putExtra(FakeAddonConfig::class, replacement)

        configuration.getExtra(FakeAddonConfig::class) shouldBeSameInstanceAs replacement
    }

    @Test
    fun `extras of different types are independent`() {
        val configuration = Configuration()
        val fake = FakeAddonConfig(enabled = true)
        val other = OtherAddonConfig(name = "x")

        configuration.putExtra(FakeAddonConfig::class, fake)
        configuration.putExtra(OtherAddonConfig::class, other)

        configuration.getExtra(FakeAddonConfig::class) shouldBeSameInstanceAs fake
        configuration.getExtra(OtherAddonConfig::class) shouldBeSameInstanceAs other
    }

    @Test
    fun `computeExtraIfAbsent creates and stores when missing then returns the same instance on subsequent reads`() {
        val configuration = Configuration()

        val first = configuration.computeExtraIfAbsent(FakeAddonConfig::class) { FakeAddonConfig() }
        val second = configuration.computeExtraIfAbsent(FakeAddonConfig::class) { FakeAddonConfig() }

        first shouldBeSameInstanceAs second
    }

    @Test
    fun `computeExtraIfAbsent does not invoke the factory when value already present`() {
        val configuration = Configuration()
        val preset = FakeAddonConfig(enabled = true)
        configuration.putExtra(FakeAddonConfig::class, preset)
        var factoryCalls = 0

        val result = configuration.computeExtraIfAbsent(FakeAddonConfig::class) {
            factoryCalls++
            FakeAddonConfig(enabled = false)
        }

        result shouldBeSameInstanceAs preset
        factoryCalls shouldBe 0
    }

    @Test
    fun `extras are isolated per Configuration instance`() {
        val a = Configuration()
        val b = Configuration()
        a.putExtra(FakeAddonConfig::class, FakeAddonConfig(enabled = true))

        b.getExtra(FakeAddonConfig::class).shouldBeNull()
    }

    @Test
    fun `reified getExtra returns null when absent`() {
        Configuration().getExtra<FakeAddonConfig>().shouldBeNull()
    }

    @Test
    fun `reified putExtra and getExtra round-trip without explicit class`() {
        val configuration = Configuration()
        val extra = FakeAddonConfig(enabled = true)

        configuration.putExtra(extra)

        configuration.getExtra<FakeAddonConfig>() shouldBeSameInstanceAs extra
    }

    @Test
    fun `reified computeExtraIfAbsent creates and reuses without explicit class`() {
        val configuration = Configuration()

        val first = configuration.computeExtraIfAbsent { FakeAddonConfig() }
        val second = configuration.computeExtraIfAbsent { FakeAddonConfig() }

        first shouldBeSameInstanceAs second
    }
}
