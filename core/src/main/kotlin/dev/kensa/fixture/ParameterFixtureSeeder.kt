package dev.kensa.fixture

import dev.kensa.parse.ElementDescriptor
import dev.kensa.parse.MethodParser
import dev.kensa.parse.RenderError
import java.lang.reflect.Method

class ParameterFixtureSeeder(private val parser: MethodParser) {

    fun seed(method: Method, arguments: Array<Any?>, fixtures: Fixtures, errors: MutableList<RenderError> = mutableListOf()) {
        val parameterFixtures = FixtureRegistry.parameterFixtures()
        if (parameterFixtures.isEmpty()) return

        // Seeding must never break an invocation. An unparseable method is still tolerated by the
        // render path (it surfaces the parse failure in the report), so a parse failure here seeds nothing.
        val descriptors = runCatching { parser.parse(method).parameters.descriptors }.getOrNull() ?: return
        parameterFixtures.forEach { fixture ->
            descriptors[fixture.from]?.let { descriptor ->
                // A parameter fixture is registered globally and seeded for every test that declares a
                // parameter of its `from` name, so a transform failure must not break an unrelated
                // invocation that happens to share that name. We swallow the throw to protect those
                // bystanders, but record it as a render error so a genuine transform bug stays visible.
                runCatching { seedOne(fixture, descriptor, arguments, fixtures) }
                    .onFailure { e -> errors += RenderError("ParameterFixture", "Could not seed parameter fixture '${fixture.key}' from parameter '${fixture.from}': ${e.message ?: e.javaClass.name}") }
            }
        }
    }

    private fun <T> seedOne(fixture: ParameterFixture<T>, descriptor: ElementDescriptor, arguments: Array<Any?>, fixtures: Fixtures) {
        fixtures.seed(fixture, fixture.deriveFrom(descriptor.resolveValue(arguments)))
    }
}
