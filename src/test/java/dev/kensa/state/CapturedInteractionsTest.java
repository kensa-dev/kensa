package dev.kensa.state;

import dev.kensa.util.Attributes;
import dev.kensa.util.KensaMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.kensa.state.CapturedInteractionBuilder.from;
import static dev.kensa.state.CapturedInteractionsTest.GOTParty.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class CapturedInteractionsTest {

    private CapturedInteractions interactions;

    @BeforeEach
    void setUp() {
        interactions = new CapturedInteractions();
    }

    @Test
    void canAddUsingInteractionBuilder() {
        String expectedKey = "Snarky comment from Ygritte to Jon";
        String expectedContent = "You know nothing Jon Snow";

        interactions.capture(from(Ygritte).to(Jon).with(expectedContent, "Snarky comment"));

        assertThat(interactions.containsKey(expectedKey))
                .withFailMessage("Expected interactions to contain key [%s] but keys were: %s", expectedKey, keysIn(interactions))
                .isTrue();

        assertThat(interactions.get(expectedKey, String.class)).isEqualTo(expectedContent);
    }

    @Test
    void canAddInteractionWithAttributes() {
        String expectedKey = "Snarky comment from Ygritte to Jon";
        String expectedContent = "You know nothing Jon Snow";

        interactions.capture(from(Ygritte).to(Jon).with(expectedContent, "Snarky comment").with(Attributes.of("language", "bolshy")));

        assertThat(interactions.containsKey(expectedKey))
                .withFailMessage("Expected interactions to contain key [%s] but keys were: %s", expectedKey, keysIn(interactions))
                .isTrue();

        assertThat(interactions.get(expectedKey, String.class)).isEqualTo(expectedContent);

        // Attributes currently only required to be Iterable to allow serialization into Json
        interactions.entrySet().forEach(entry -> entry.attributes().forEach(attribute -> {
            assertThat(attribute.name()).isEqualTo("language");
            assertThat(attribute.value()).isEqualTo("bolshy");
        }));
    }

    @Test
    void incrementsSequenceNumberForDuplicateKeys() {
        interactions.capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"));
        interactions.capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"));
        interactions.capture(from(NightKing).to(Daenerys).with("Here's that zombie dragon you ordered!", "Snarky comment"));


        assertThat(interactions.containsKey("Snarky comment from NightKing to Daenerys")).isTrue();
        assertThat(interactions.containsKey("Snarky comment 1 from NightKing to Daenerys")).isTrue();
        assertThat(interactions.containsKey("Snarky comment 2 from NightKing to Daenerys")).isTrue();
    }

    @Test
    void canAddInteractionWithGroup() {
        interactions.capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"));

        assertThat(interactions.containsKey("(Temporary) Defiant statement from Jon to Daenerys")).isTrue();
    }

    @Test
    void incrementsSequenceNumberForDuplicateKeysWithGroup() {
        interactions.capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"));
        interactions.capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"));
        interactions.capture(from(Jon).to(Daenerys).group("Temporary").with("I will not bend the knee", "Defiant statement"));

        assertThat(interactions.containsKey("(Temporary) Defiant statement from Jon to Daenerys")).isTrue();
        assertThat(interactions.containsKey("(Temporary) Defiant statement 1 from Jon to Daenerys")).isTrue();
        assertThat(interactions.containsKey("(Temporary) Defiant statement 2 from Jon to Daenerys")).isTrue();
    }

    private List<String> keysIn(CapturedInteractions interactions) {
        return interactions.entrySet()
                .stream()
                .map(KensaMap.Entry::key)
                .collect(toList());
    }

    enum GOTParty implements Party {
        Daenerys,
        Jon,
        Ygritte,
        NightKing;

        @Override
        public String asString() {
            return this.name();
        }
    }
}