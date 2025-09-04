package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.Acronym.Companion.of
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class DictionaryTest {
    private lateinit var dictionary: Dictionary
    private val defaultEmphasis = EmphasisDescriptor.Default

    @BeforeEach
    fun setUp() {
        dictionary = Dictionary()
    }

    @AfterEach
    fun tearDown() {
        dictionary.clearAcronyms()
    }

    @Test
    fun canStreamExistingAcronyms() {
        val acronyms = arrayOf(
                of("foo", "means foo"),
                of("boo", "means boo"),
                of("moo", "means moo")
        )
        dictionary.putAcronyms(*acronyms)
        dictionary.acronyms shouldContainAll acronyms.toList()
    }

    @Test
    fun canPutKeywords() {
        dictionary.putKeyword("foo")
        dictionary.putKeywords("boo", "moo")
        dictionary.keywords shouldContainAll listOf("foo", "boo", "moo").map { Keyword(it) }
    }

    @Test
    fun canClearAcronyms() {
        dictionary.putAcronyms(of("foo", "foo"))
        dictionary.clearAcronyms()
        dictionary.acronyms.shouldBeEmpty()
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun throwsOnAttemptToAddInvalidKeyword(keyword: String) {
        shouldThrowExactly<IllegalArgumentException> { dictionary.putKeyword(keyword) }
        shouldThrowExactly<IllegalArgumentException> { dictionary.putKeywords("foo", keyword) }
    }

    @Test
    fun `indexProtectedPhrases finds singular form of protected phrase`() {
        dictionary.putProtectedPhrases(ProtectedPhrase("cat"))
        
        val sentence = "I have a cat at home"
        val indices = dictionary.indexProtectedPhrases(sentence)
        
        indices shouldHaveSize 1
        indices[0].start shouldBe 9  // Start index of "cat"
        indices[0].end shouldBe 12  // End index of "cat"
        indices[0].emphasisDescriptor shouldBe defaultEmphasis
    }
    
    @Test
    fun `indexProtectedPhrases finds plural form of protected phrase with regular plural`() {
        dictionary.putProtectedPhrases(ProtectedPhrase("cat"))
        
        val sentence = "Ihavetwocatsathome"
        val indices = dictionary.indexProtectedPhrases(sentence)

        indices shouldHaveSize 1
        indices[0].start shouldBe 8  // Start index of "cats"
        indices[0].end shouldBe 12  // End index of "cats" (exclusive end index)
        indices[0].emphasisDescriptor shouldBe defaultEmphasis
    }

    @Test
    fun `indexProtectedPhrases ignores fake plural form of protected phrase made with CamelCased words`() {
        dictionary.putProtectedPhrases(ProtectedPhrase("p2p"))

        val sentence = "thereIsAp2pSubnetInHere"

        val indices = dictionary.indexProtectedPhrases(sentence)

        indices shouldHaveSize 1
        indices[0].start shouldBe 8
        indices[0].end shouldBe 11
        indices[0].emphasisDescriptor shouldBe defaultEmphasis
    }

    @Test
    fun `indexProtectedPhrases finds plural form of protected phrase ending in y`() {
        dictionary.putProtectedPhrases(ProtectedPhrase("city"))
        
        val sentence = "There are many cities in the world"
        val indices = dictionary.indexProtectedPhrases(sentence)
        
        indices shouldHaveSize 1
        indices[0].start shouldBe 15  // Start index of "cities"
        indices[0].end shouldBe 21  // End index of "cities" (exclusive end index)
        indices[0].emphasisDescriptor shouldBe defaultEmphasis
    }
    
    @Test
    fun `indexProtectedPhrases finds plural form of protected phrase ending in s, x, z, ch, sh`() {
        dictionary.putProtectedPhrases(
            ProtectedPhrase("box"),
            ProtectedPhrase("bus"),
            ProtectedPhrase("buzz"),
            ProtectedPhrase("match"),
            ProtectedPhrase("dish")
        )
        
        val sentence1 = "I have many boxes in my room"
        val indices1 = dictionary.indexProtectedPhrases(sentence1)
        
        val sentence2 = "There are buses on the road"
        val indices2 = dictionary.indexProtectedPhrases(sentence2)
        
        val sentence3 = "The buzzes are loud"
        val indices3 = dictionary.indexProtectedPhrases(sentence3)
        
        val sentence4 = "The matches are in the drawer"
        val indices4 = dictionary.indexProtectedPhrases(sentence4)
        
        val sentence5 = "The dishes are clean"
        val indices5 = dictionary.indexProtectedPhrases(sentence5)
        
        indices1 shouldHaveSize 1
        indices1[0].start shouldBe 12  // Start index of "boxes"
        indices1[0].end shouldBe 17  // End index of "boxes" (exclusive end index)
        
        indices2 shouldHaveSize 1
        indices2[0].start shouldBe 10  // Start index of "buses"
        indices2[0].end shouldBe 15  // End index of "buses" (exclusive end index)
        
        indices3 shouldHaveSize 1
        indices3[0].start shouldBe 4  // Start index of "buzzes"
        indices3[0].end shouldBe 10  // End index of "buzzes" (exclusive end index)
        
        indices4 shouldHaveSize 1
        indices4[0].start shouldBe 4  // Start index of "matches"
        indices4[0].end shouldBe 11  // End index of "matches" (exclusive end index)
        
        indices5 shouldHaveSize 1
        indices5[0].start shouldBe 4  // Start index of "dishes"
        indices5[0].end shouldBe 10  // End index of "dishes" (exclusive end index)
    }
    
    @Test
    fun `indexProtectedPhrases finds both singular and plural forms in the same sentence`() {
        dictionary.putProtectedPhrases(ProtectedPhrase("dog"))
        
        val sentence = "I have a dog and two dogs at home"
        val indices = dictionary.indexProtectedPhrases(sentence)
        
        indices shouldHaveSize 2
        indices[0].start shouldBe 9  // Start index of "dog"
        indices[0].end shouldBe 12  // End index of "dog" (exclusive end index)

        indices[1].start shouldBe 21  // Start index of "dogs"
        indices[1].end shouldBe 25  // End index of "dogs" (exclusive end index)
    }
    
    @Test
    fun `indexProtectedPhrases finds multiple occurrences of protected phrases`() {
        dictionary.putProtectedPhrases(
            ProtectedPhrase("cat"),
            ProtectedPhrase("dog")
        )
        
        val sentence = "I have cats and dogs, a cat and a dog"
        val indices = dictionary.indexProtectedPhrases(sentence)
        
        indices shouldHaveSize 4
        
        val sortedIndices = indices.sortedBy { it.start }
        
        sortedIndices[0].start shouldBe 7   // Start index of "cats"
        sortedIndices[0].end shouldBe 11  // End index of "cats" (exclusive end index)
        sortedIndices[0].emphasisDescriptor shouldBe defaultEmphasis

        sortedIndices[1].start shouldBe 16   // Start index of "dogs"
        sortedIndices[1].end shouldBe 20  // End index of "dogs" (exclusive end index)
        sortedIndices[1].emphasisDescriptor shouldBe defaultEmphasis

        sortedIndices[2].start shouldBe 24   // Start index of "cat"
        sortedIndices[2].end shouldBe 27  // End index of "cat" (exclusive end index)
        sortedIndices[2].emphasisDescriptor shouldBe defaultEmphasis
        
        sortedIndices[3].start shouldBe 34   // Start index of "dog"
        sortedIndices[3].end shouldBe 37  // End index of "dog" (exclusive end index)
        sortedIndices[3].emphasisDescriptor shouldBe defaultEmphasis
    }

    companion object {
        @JvmStatic
        fun arguments(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(""),
                    Arguments.of("   "),
                    Arguments.of("x"),
                    Arguments.of("xx")
            )
        }
    }
}