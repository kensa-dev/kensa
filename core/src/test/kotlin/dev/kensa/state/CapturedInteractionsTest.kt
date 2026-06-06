package dev.kensa.state

import dev.kensa.render.diagram.directive.ArrowStyle
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.state.CapturedInteractionsTest.GOTParty.*
import dev.kensa.util.Attributes
import dev.kensa.util.KensaMap
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CapturedInteractionsTest {
    private lateinit var interactions: CapturedInteractions

    @BeforeEach
    fun setUp() {
        interactions = CapturedInteractions(SetupStrategy.Ungrouped)
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

            containsKey(expectedKey).shouldBeTrue()
            get<String>(expectedKey) shouldBe expectedContent
        }
    }

    @Test
    fun canAddInteractionWithAttributes() {
        val expectedKey = "Snarky comment from Ygritte to Jon"
        val expectedContent = "You know nothing Jon Snow"

        with(interactions) {
            capture(from(Ygritte).to(Jon).with(expectedContent, "Snarky comment").with(Attributes.of("language", "bolshy")))

            containsKey(expectedKey).shouldBeTrue()
            get<String>(expectedKey) shouldBe expectedContent

            // Attributes currently only required to be Iterable to allow serialization into Json
            entrySet().forEach { entry: KensaMap.Entry ->
                with(entry.attributes) {
                    arrowStyle shouldBe ArrowStyle.UmlSynchronous
                    get<String>("language") shouldBe "bolshy"
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

            containsKey("Snarky comment from NightKing to Daenerys").shouldBeTrue()
            containsKey("Snarky comment 1 from NightKing to Daenerys").shouldBeTrue()
            containsKey("Snarky comment 2 from NightKing to Daenerys").shouldBeTrue()
        }
    }

    @Test
    fun canAddInteractionWithGroup() {
        interactions.capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
        interactions.containsKey("Defiant statement from Jon to Daenerys").shouldBeTrue()
    }

    @Test
    fun incrementsSequenceNumberForDuplicateKeysWithGroup() {
        with(interactions) {
            capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
            capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
            capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"))
            containsKey("Defiant statement from Jon to Daenerys").shouldBeTrue()
            containsKey("Defiant statement 1 from Jon to Daenerys").shouldBeTrue()
            containsKey("Defiant statement 2 from Jon to Daenerys").shouldBeTrue()
        }
    }

    @Test
    fun canSetAndUnsetUnderTest() {
        with(interactions) {
            isUnderTest = true
            capture(from(Jon).to(Daenerys).with("I will not bend the knee", "Defiant statement"))
            interactions.containsKey("Defiant statement from Jon to Daenerys").shouldBeTrue()
            isUnderTest = false
            capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"))
            containsKey("Snarky comment from NightKing to Daenerys").shouldBeTrue()
        }
    }

    @Test
    fun explicitlySetGroupOverridesUnderTestTestGroup() {
        with(interactions) {
            isUnderTest = true
            capture(from(Jon).to(Daenerys).group("Override").with("I will not bend the knee", "Defiant statement"))

            containsKey("Defiant statement from Jon to Daenerys").shouldBeTrue()
        }
    }

    @Test
    fun canDisableUnderTest() {
        with(interactions) {
            isUnderTestEnabled = false
            isUnderTest = true
            capture(from(Jon).to(Daenerys).with("I will not bend the knee", "Defiant statement"))

            containsKey("Defiant statement from Jon to Daenerys").shouldBeTrue()
        }
    }

    @Test
    fun `inbound seam derives the to-party from its owner`() {
        with(interactions) {
            capture(from(Ygritte).seam(Inbound("got:raven", "Raven", Jon)).with("content", "Raven"))
            containsKey("Raven from Ygritte to Jon").shouldBeTrue()
        }
    }

    @Test
    fun `seam is recorded as an interaction attribute`() {
        val seam = Inbound("got:raven", "Raven", Jon)
        with(interactions) {
            capture(from(Ygritte).seam(seam).with("content", "Raven"))
            entrySet().single().attributes.get<SeamDefinition>(Attributes.Key.Seam) shouldBe seam
        }
    }

    @Test
    fun `outbound seam requires the from-party to be its owner`() {
        shouldThrow<IllegalArgumentException> {
            from(Ygritte).seam(Outbound("got:decree", "Royal Decree", Jon))
        }
    }

    @Test
    fun `inbound seam rejects a pre-set to-party that does not match its owner`() {
        shouldThrow<IllegalArgumentException> {
            from(Ygritte).to(Daenerys).seam(Inbound("got:raven", "Raven", Jon))
        }
    }

    @Test
    fun `outbound seam records interaction when from-party matches owner`() {
        val seam = Outbound("got:decree", "Royal Decree", Jon)
        with(interactions) {
            capture(from(Jon).to(Daenerys).seam(seam).with("content", "Royal Decree"))
            entrySet().single().attributes.get<SeamDefinition>(Attributes.Key.Seam) shouldBe seam
        }
    }

    internal enum class GOTParty : Party {
        Daenerys, Jon, Ygritte, NightKing;

        override fun asString(): String = name
    }
}