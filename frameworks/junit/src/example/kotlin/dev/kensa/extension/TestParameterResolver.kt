package dev.kensa.extension

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class TestParameterResolver : ParameterResolver {

    class MyArgument(val value: String)

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext) =
        parameterContext.parameter.type == MyArgument::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any =
        MyArgument(MY_PARAMETER_VALUE)

    companion object {
        const val MY_PARAMETER_VALUE = "myParameter"
    }
}