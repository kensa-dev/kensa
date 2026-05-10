package dev.kensa.hamkrest.testsupport.field.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.natpryce.hamkrest.MatchResult
import dev.kensa.hamkrest.testsupport.field.matching
import dev.kensa.hamkrest.testsupport.field.of
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class JsonFieldTest {

    private val mapper = ObjectMapper()

    private val sample = mapper.readTree(
        """
        {
            "id": "abc",
            "count": 42,
            "size": 9000000000,
            "active": true,
            "address": { "city": "Bristol" },
            "tags": ["alpha", "beta"],
            "items": [
                { "code": "A", "name": "Apple" },
                { "code": "B", "name": "Banana" }
            ]
        }
        """
    )

    @Test
    fun `JsonField extracts via supplied function`() {
        val field = JsonField("/id") { node, path -> node.at(path).textValue() }
        field.extract(sample) shouldBe "abc"
    }

    @Test
    fun `JsonField path is exposed publicly`() {
        val field = JsonTextField("/id")
        field.path shouldBe "/id"
    }

    @Test
    fun `JsonField name and description default to path`() {
        val field = JsonTextField("/id")
        field.name shouldBe "/id"
        field.description shouldBe "/id"
    }

    @Test
    fun `JsonTextField extracts string value at JsonPointer`() {
        JsonTextField("/id").extract(sample) shouldBe "abc"
    }

    @Test
    fun `JsonTextField returns null when path missing`() {
        JsonTextField("/missing").extract(sample) shouldBe null
    }

    @Test
    fun `JsonIntField extracts int from numeric node`() {
        JsonIntField("/count").extract(sample) shouldBe 42
    }

    @Test
    fun `JsonLongField extracts long from numeric node`() {
        JsonLongField("/size").extract(sample) shouldBe 9_000_000_000L
    }

    @Test
    fun `JsonBooleanField extracts boolean`() {
        JsonBooleanField("/active").extract(sample) shouldBe true
    }

    @Test
    fun `JsonNodeField extracts the node at the path`() {
        val node = JsonNodeField("/address").extract(sample)
        node?.get("city")?.textValue() shouldBe "Bristol"
    }

    @Test
    fun `ArrayNodeField extracts array node at path`() {
        val node: ArrayNode? = ArrayNodeField("/tags").extract(sample)
        node?.size() shouldBe 2
    }

    @Test
    fun `ArrayNodeField returns null when path is missing`() {
        ArrayNodeField("/missing").extract(sample) shouldBe null
    }

    @Test
    fun `JsonField composes with of via core extension`() {
        val field = JsonTextField("/id")
        (field of "abc").invoke(sample) shouldBe MatchResult.Match
        val miss = field of "xyz"
        miss.invoke(sample).shouldBeInstanceOf<MatchResult.Mismatch>()
        miss.description shouldStartWith "/id:"
    }

    @Test
    fun `JsonField composes with matching via core extension`() {
        val field = JsonTextField("/id")
        (field matching "[a-z]+").invoke(sample) shouldBe MatchResult.Match
    }

    @Test
    fun `JsonMapField returns map keyed by named attribute of array elements`() {
        val map = JsonMapField("/items", "code").extract(sample)
        map?.keys shouldBe setOf("A", "B")
        map?.get("A")?.get("name")?.textValue() shouldBe "Apple"
    }

    @Test
    fun `JsonMapField name and description default to path`() {
        val field = JsonMapField("/items", "code")
        field.name shouldBe "/items"
        field.description shouldBe "/items"
    }

    @Test
    fun `JsonMapKeyField extracts transformed value from map`() {
        val nameKey = JsonMapKeyField("name") { it.textValue() }
        nameKey.keyName shouldBe "name"
        val a = mapOf("name" to mapper.readTree(""""Apple""""))
        nameKey.extract(a) shouldBe "Apple"
        nameKey.extract(emptyMap()) shouldBe null
    }

    @Test
    fun `transformString builds extractor that maps text to typed value`() {
        val extractor = transformString { it.uppercase() }
        extractor(sample, "/id") shouldBe "ABC"
        extractor(sample, "/missing") shouldBe null
    }

    @Test
    fun `transformInt builds extractor that maps int to typed value`() {
        val extractor = transformInt { it + 1 }
        extractor(sample, "/count") shouldBe 43
    }

    @Test
    fun `transformLong builds extractor that maps long to typed value`() {
        val extractor = transformLong { it / 1_000_000_000L }
        extractor(sample, "/size") shouldBe 9L
    }

    @Test
    fun `transformBoolean builds extractor that maps bool to typed value`() {
        val extractor = transformBoolean { !it }
        extractor(sample, "/active") shouldBe false
    }

    @Test
    fun `textValueOrNullAt returns text or null`() {
        sample.textValueOrNullAt("/id") shouldBe "abc"
        sample.textValueOrNullAt("/missing") shouldBe null
    }

    @Test
    fun `intValueOrNullAt returns int or null`() {
        sample.intValueOrNullAt("/count") shouldBe 42
        sample.intValueOrNullAt("/id") shouldBe null
    }

    @Test
    fun `longValueOrNullAt returns long or null`() {
        sample.longValueOrNullAt("/size") shouldBe 9_000_000_000L
        sample.longValueOrNullAt("/id") shouldBe null
    }

    @Test
    fun `booleanValueOrNullAt returns boolean from boolean node`() {
        sample.booleanValueOrNullAt("/active") shouldBe true
    }

    @Test
    fun `nodeOrNullAt returns node when present and non-null`() {
        val node: JsonNode? = sample.nodeOrNullAt("/address")
        node?.get("city")?.textValue() shouldBe "Bristol"
    }

    @Test
    fun `nodeOrNullAt returns null for missing path`() {
        sample.nodeOrNullAt("/missing") shouldBe null
    }

    @Test
    fun `arrayNodeAt returns ArrayNode at path`() {
        val node: ArrayNode = sample.arrayNodeAt("/tags")
        node.size() shouldBe 2
    }
}
