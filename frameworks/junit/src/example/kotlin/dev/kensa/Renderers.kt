package dev.kensa

import dev.kensa.extension.TestParameterResolver
import dev.kensa.render.ValueRenderer

object MyArgumentRenderer : ValueRenderer<TestParameterResolver.MyArgument> {
    override fun render(value: TestParameterResolver.MyArgument): String = value.value
}