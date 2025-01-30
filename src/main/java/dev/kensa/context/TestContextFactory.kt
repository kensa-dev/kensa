package dev.kensa.context

import dev.kensa.Kensa
import dev.kensa.UseSetupStrategy
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy
import dev.kensa.util.findAnnotation
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.reflect.AnnotatedElement

class TestContextFactory {
    fun createFor(context: ExtensionContext): TestContext =
        TestContext(Givens(), CapturedInteractions(context.requiredTestMethod.setupStrategy(context.requiredTestClass.setupStrategy(Kensa.configuration.setupStrategy))))

    private fun AnnotatedElement.setupStrategy(default: SetupStrategy): SetupStrategy = findAnnotation<UseSetupStrategy>()?.value ?: default
}