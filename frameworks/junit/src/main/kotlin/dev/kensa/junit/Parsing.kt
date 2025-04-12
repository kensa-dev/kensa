package dev.kensa.junit

import dev.kensa.parse.Java20Parser
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.kotlin.KotlinParserDelegate.Companion.findAnnotationNames

private val testAnnotationNames = listOf("Test", "org.junit.jupiter.api.Test", "ParameterizedTest", "org.junit.jupiter.params.ParameterizedTest")

val isJavaClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean = { context ->
    context.methodModifier().any { it.annotation().hasTestAnnotation() }
}

val isJavaInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean = { context ->
    context.interfaceMethodModifier().any { it.annotation().hasTestAnnotation()  }
}

fun Java20Parser.AnnotationContext?.hasTestAnnotation(): Boolean =
    (this?.markerAnnotation()?.typeName()?.text?.let {
        testAnnotationNames.contains(it)
    } ?: this?.normalAnnotation()?.typeName()?.text?.let {
        testAnnotationNames.contains(it)
    }) == true

val isKotlinTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { context ->
    context.findAnnotationNames().any { name -> testAnnotationNames.contains(name) }
}