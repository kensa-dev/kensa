package dev.kensa.sentence

import dev.kensa.parse.Location
import dev.kensa.sentence.TemplateToken.ExpandableTemplateToken
import dev.kensa.sentence.TemplateToken.Type.Expandable
import dev.kensa.sentence.TemplateToken.Type.Keyword
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

internal class SentenceBuilderTest {

    private fun builderAt(line: Int = 1) = SentenceBuilder(false, Location(line, 0), Dictionary(), 4)

    @Test
    internal fun `emits keyword token before expandable header when placeholder starts with a keyword`() {
        val builder = builderAt()
        builder.beginExpandableSentence(Location(1, 0), "thenAllTheVerificationsAreCorrect", emptyList())
        builder.finishExpandable()

        val tokens = builder.build().tokens
        tokens[0].types shouldContain Keyword
        tokens[0].template shouldBe "Then"
        val expandable = tokens[1].shouldBeInstanceOf<ExpandableTemplateToken>()
        expandable.types shouldContain Expandable
        expandable.template shouldBe "All The Verifications Are Correct"
        expandable.name shouldBe "thenAllTheVerificationsAreCorrect"
    }

    @Test
    internal fun `normalises whenever to when in the keyword token`() {
        val builder = builderAt()
        builder.beginExpandableSentence(Location(1, 0), "wheneverTheActionOccurs", emptyList())
        builder.finishExpandable()

        val tokens = builder.build().tokens
        tokens[0].types shouldContain Keyword
        tokens[0].template shouldBe "When"
        tokens[1].shouldBeInstanceOf<ExpandableTemplateToken>().template shouldBe "The Action Occurs"
    }

    @Test
    internal fun `does not emit keyword token when placeholder has no leading keyword`() {
        val builder = builderAt()
        builder.beginExpandableSentence(Location(1, 0), "someActionOccurs", emptyList())
        builder.finishExpandable()

        val tokens = builder.build().tokens
        tokens[0].shouldBeInstanceOf<ExpandableTemplateToken>().template shouldBe "some Action Occurs"
    }

    @Test
    internal fun `does not emit keyword token when expandable is not first in sentence`() {
        val builder = builderAt()
        builder.append(dev.kensa.parse.LocatedEvent.Identifier(Location(1, 0), "given"))
        builder.beginExpandableSentence(Location(1, 6), "thenSomethingHappens", emptyList())
        builder.finishExpandable()

        val tokens = builder.build().tokens
        val expandable = tokens.filterIsInstance<ExpandableTemplateToken>().single()
        expandable.template shouldBe "then Something Happens"
    }
}
