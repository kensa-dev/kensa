package dev.kensa.fixture

import dev.kensa.Configuration
import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.example.KotlinWithParameters
import dev.kensa.parse.CompositeParserDelegate
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.parse.RenderError
import dev.kensa.parse.kotlin.KotlinParser
import dev.kensa.parse.kotlin.KotlinParserDelegate
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import kotlin.io.path.Path

class ParameterFixtureSeederTest {

    @BeforeEach
    fun setup() {
        FixtureRegistry.clearFixtures()
        ExpandableInvocationContextHolder.bindToCurrentThread(ExpandableInvocationContext())
    }

    @AfterEach
    fun tearDown() {
        ExpandableInvocationContextHolder.clearFromThread()
    }

    @Test
    fun `seeds the transformed parameter value under the fixture key`() {
        FixtureRegistry.registerFixtures(SeederFixtures)

        val configuration = Configuration().apply {
            sourceLocations = listOf(Path("src/example/kotlin"))
        }
        val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == "parameterizedTest" }
        val parser = MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(KotlinParserDelegate(isTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
            )
        )

        val method = KotlinWithParameters::class.java.declaredMethods.first { it.name == "parameterizedTest" }
        val fixtures = Fixtures()

        ParameterFixtureSeeder(parser).seed(method, arrayOf<Any?>("alice", 1), fixtures)

        fixtures[SeederFixtures.greeting] shouldBe "Hello, alice"
    }

    @Test
    fun `does not throw and seeds nothing when the method cannot be parsed`() {
        FixtureRegistry.registerFixtures(SeederFixtures)

        val parser: MethodParser = mock {
            on { parse(any()) } doThrow RuntimeException("could not parse")
        }
        val method = KotlinWithParameters::class.java.declaredMethods.first { it.name == "parameterizedTest" }
        val fixtures = Fixtures()

        shouldNotThrowAny {
            ParameterFixtureSeeder(parser).seed(method, arrayOf<Any?>("alice", 1), fixtures)
        }

        fixtures.specs().shouldBeEmpty()
    }

    @Test
    fun `records a render error and does not throw when a fixture transform fails`() {
        FixtureRegistry.registerFixtures(ThrowingSeederFixtures)

        val configuration = Configuration().apply {
            sourceLocations = listOf(Path("src/example/kotlin"))
        }
        val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == "parameterizedTest" }
        val parser = MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(KotlinParserDelegate(isTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
            )
        )

        val method = KotlinWithParameters::class.java.declaredMethods.first { it.name == "parameterizedTest" }
        val fixtures = Fixtures()
        val errors = mutableListOf<RenderError>()

        shouldNotThrowAny {
            ParameterFixtureSeeder(parser).seed(method, arrayOf<Any?>("alice", 1), fixtures, errors)
        }

        fixtures.specs().shouldBeEmpty()
        errors.shouldHaveSize(1)
        errors.first().type shouldBe "ParameterFixture"
        errors.first().message shouldContain "throwing"
    }

    @Test
    fun `seeds matching fixtures and skips those whose parameter name is absent`() {
        FixtureRegistry.registerFixtures(MixedSeederFixtures)

        val configuration = Configuration().apply {
            sourceLocations = listOf(Path("src/example/kotlin"))
        }
        val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == "parameterizedTest" }
        val parser = MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(KotlinParserDelegate(isTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
            )
        )

        val method = KotlinWithParameters::class.java.declaredMethods.first { it.name == "parameterizedTest" }
        val fixtures = Fixtures()

        ParameterFixtureSeeder(parser).seed(method, arrayOf<Any?>("alice", 1), fixtures)

        fixtures[MixedSeederFixtures.greeting] shouldBe "Hello, alice"
        fixtures.specs().map { it.key } shouldBe listOf("greeting")
    }

    @Test
    fun `secondary fixture derives from a seeded parameter fixture`() {
        FixtureRegistry.registerFixtures(SecondaryFromParameterFixtures)

        val configuration = Configuration().apply {
            sourceLocations = listOf(Path("src/example/kotlin"))
        }
        val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == "parameterizedTest" }
        val parser = MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(KotlinParserDelegate(isTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
            )
        )

        val method = KotlinWithParameters::class.java.declaredMethods.first { it.name == "parameterizedTest" }
        val fixtures = Fixtures()

        ParameterFixtureSeeder(parser).seed(method, arrayOf<Any?>("alice", 1), fixtures)

        fixtures[SecondaryFromParameterFixtures.shouted] shouldBe "HELLO, ALICE!"
        fixtures.specs() shouldBe listOf(
            FixtureSpec("greeting", emptyList()),
            FixtureSpec("shouted", listOf("greeting"))
        )
    }

    @Suppress("unused")
    object SeederFixtures : FixtureContainer {
        val greeting = parameterFixture("greeting", from = "first") { name: String -> "Hello, $name" }
    }

    @Suppress("unused")
    object ThrowingSeederFixtures : FixtureContainer {
        val throwing = parameterFixture<String, String>("throwing", from = "first") { throw RuntimeException("boom") }
    }

    @Suppress("unused")
    object SecondaryFromParameterFixtures : FixtureContainer {
        val greeting = parameterFixture("greeting", from = "first") { name: String -> "Hello, $name" }
        val shouted = fixture("shouted", greeting) { message: String -> "${message.uppercase()}!" }
    }

    @Suppress("unused")
    object MixedSeederFixtures : FixtureContainer {
        val greeting = parameterFixture("greeting", from = "first") { name: String -> "Hello, $name" }
        val orphan = parameterFixture("orphan", from = "doesNotExist") { value: String -> value.uppercase() }
    }
}
