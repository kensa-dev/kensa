package dev.kensa.state

import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.CapturedInteractionsTest.GOTParty.*
import dev.kensa.util.Attributes.Companion.of
import dev.kensa.util.KensaMap
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CapturedInteractionsTest {
    private lateinit var interactions: CapturedInteractions

    @BeforeEach
    fun setUp() {
        interactions = CapturedInteractions()
    }

    @Test
    fun canTestWhetherInteractionsContainsEntriesMatchingPredicate() {
        with(interactions) {
            put("Foo", "Moo")
            assertTrue(containsEntriesMatching { entry: KensaMap.Entry -> entry.key == "Foo" })
            assertFalse(containsEntriesMatching { entry: KensaMap.Entry -> entry.key == "Moo" })
        }
    }

    @Test
    fun canAddUsingInteractionBuilder() {
        val expectedKey = "Snarky comment from Ygritte to Jon"
        val expectedContent = "You know nothing Jon Snow"

        with(interactions) {
            capture(from(Ygritte).to(Jon).with(expectedContent, "Snarky comment"))
            assertThat(containsKey(expectedKey))
                    .withFailMessage("Expected interactions to contain key [%s] but keys were: %s", expectedKey, keysIn(interactions))
                    .isTrue
            assertThat(get<String>(expectedKey)).isEqualTo(expectedContent)
        }
    }

    @Test
    fun canAddInteractionWithAttributes() {
        val expectedKey = "Snarky comment from Ygritte to Jon"
        val expectedContent = "You know nothing Jon Snow"

        with(interactions) {
            capture(from(Ygritte).to(Jon).with(expectedContent, "Snarky comment").with(of("language", "bolshy")))

            assertThat(containsKey(expectedKey))
                    .withFailMessage("Expected interactions to contain key [%s] but keys were: %s", expectedKey, keysIn(interactions))
                    .isTrue
            assertThat(get<String>(expectedKey)).isEqualTo(expectedContent)

            // Attributes currently only required to be Iterable to allow serialization into Json
            entrySet().forEach { entry: KensaMap.Entry ->
                entry.attributes.forEach { (name, value) ->
                    assertThat(name).isEqualTo("language")
                    assertThat(value).isEqualTo("bolshy")
                }
            }
        }
    }

    @Test
    fun incrementsSequenceNumberForDuplicateKeys() {
        with(interactions) {
            capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"))
            capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"))
            capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"))

            assertThat(containsKey("Snarky comment from NightKing to Daenerys")).isTrue
            assertThat(containsKey("Snarky comment 1 from NightKing to Daenerys")).isTrue
            assertThat(containsKey("Snarky comment 2 from NightKing to Daenerys")).isTrue
        }
    }

    @Test
    fun canAddInteractionWithGroup() {
        interactions.capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
        assertThat(interactions.containsKey("(Temporary) Defiant statement from Jon to Daenerys")).isTrue
    }

    @Test
    fun incrementsSequenceNumberForDuplicateKeysWithGroup() {
        with(interactions) {
            capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
            capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
            capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
            assertThat(containsKey("(Temporary) Defiant statement from Jon to Daenerys")).isTrue
            assertThat(containsKey("(Temporary) Defiant statement 1 from Jon to Daenerys")).isTrue
            assertThat(containsKey("(Temporary) Defiant statement 2 from Jon to Daenerys")).isTrue
        }
    }

    @Test
    fun canSetAndUnsetUnderTest() {
        with(interactions) {
            isUnderTest = true
            capture(from(Jon).to(Daenerys).with("I will not bend the knee", "Defiant statement"))
            assertThat(interactions.containsKey("(Test) Defiant statement from Jon to Daenerys")).isTrue
            isUnderTest = false
            capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"))
            assertThat(containsKey("Snarky comment from NightKing to Daenerys")).isTrue
        }
    }

    @Test
    fun explicitlySetGroupOverridesUnderTestTestGroup() {
        with(interactions) {
            isUnderTest = true
            capture(from(Jon).to(Daenerys).group("Override").with("I will not bend the knee", "Defiant statement"))

            assertThat(containsKey("(Override) Defiant statement from Jon to Daenerys")).isTrue
        }
    }

    @Test
    fun canDisableUnderTest() {
        with(interactions) {
            disableUnderTest()
            isUnderTest = true
            capture(from(Jon).to(Daenerys).with("I will not bend the knee", "Defiant statement"))

            assertThat(containsKey("Defiant statement from Jon to Daenerys")).isTrue
        }
    }

    private fun keysIn(interactions: CapturedInteractions): List<String> = interactions.entrySet()
            .map(KensaMap.Entry::key)
            .toList()

    internal enum class GOTParty : Party {
        Daenerys, Jon, Ygritte, NightKing;

        override fun asString(): String = name
    }
}