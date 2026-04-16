package dev.kensa.parse

import dev.kensa.parse.Event.*
import dev.kensa.parse.LocatedEvent.EnterStatement
import dev.kensa.sentence.Dictionary
import dev.kensa.sentence.SentenceBuilder
import dev.kensa.sentence.TemplateToken.ErrorTemplateToken
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ParserStateMachineRecoveryTest {

    private val location10 = Location(lineNumber = 10, linePosition = 4)
    private val location20 = Location(lineNumber = 20, linePosition = 4)
    private val location30 = Location(lineNumber = 30, linePosition = 4)

    private fun createSentenceBuilder(isNote: Boolean, location: Location): SentenceBuilder =
        SentenceBuilder(isNote, location, Dictionary(), tabSize = 4)

    private fun newStateMachine() = ParserStateMachine(::createSentenceBuilder)

    @Test
    fun `failed statement produces error in parseErrors and error sentence`() {
        val sm = newStateMachine()

        sm.apply(EnterMethod)
        sm.apply(EnterStatement(location10))
        sm.apply(ExitBlock)
        sm.apply(ExitStatement)

        sm.parseErrors shouldHaveSize 1
        sm.parseErrors[0].lineNumber shouldBe 10

        sm.sentences shouldHaveSize 1
        val token = sm.sentences[0].tokens.single()
        val errorToken = token.shouldBeInstanceOf<ErrorTemplateToken>()
        errorToken.message shouldBe "Could not parse this statement"
        sm.sentences[0].lineNumber shouldBe 10
    }

    @Test
    fun `nested EnterStatement during skip increments depth correctly`() {
        val sm = newStateMachine()

        sm.apply(EnterMethod)
        sm.apply(EnterStatement(location10))
        sm.apply(ExitBlock) // bad transition → skipDepth = 1

        sm.apply(EnterStatement(location20))
        sm.apply(ExitStatement)

        sm.sentences shouldHaveSize 0

        sm.apply(ExitStatement)

        sm.parseErrors shouldHaveSize 1
        sm.sentences shouldHaveSize 1
        sm.sentences[0].tokens.single().shouldBeInstanceOf<ErrorTemplateToken>()
    }

    @Test
    fun `statements after failed statement parse normally`() {
        val sm = newStateMachine()

        sm.apply(EnterMethod)
        sm.apply(EnterBlock)

        sm.apply(EnterStatement(location10))
        sm.apply(ExitBlock) // bad transition → recovery
        sm.apply(ExitStatement) // recovery — state reset to TestBlock

        sm.apply(EnterStatement(location20))
        sm.apply(ExitStatement)

        sm.parseErrors shouldHaveSize 1
        sm.sentences shouldHaveSize 2
        sm.sentences[0].tokens.single().shouldBeInstanceOf<ErrorTemplateToken>()
        sm.sentences[1].tokens.none { it is ErrorTemplateToken } shouldBe true
        sm.sentences[1].lineNumber shouldBe 20
    }
}
