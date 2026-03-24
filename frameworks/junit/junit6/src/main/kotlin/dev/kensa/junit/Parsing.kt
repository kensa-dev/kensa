package dev.kensa.junit

import dev.kensa.context.FrameworkDescriptor
import dev.kensa.parse.java.Java20Parser
import dev.kensa.parse.kotlin.KotlinParser
import dev.kensa.parse.kotlin.KotlinParserDelegate.Companion.findAnnotationNames
import dev.kensa.state.TestState.NotExecuted
import dev.kensa.util.findAnnotation
import dev.kensa.util.findTestMethods
import dev.kensa.util.hasAnnotation
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.lang.reflect.Method
import dev.kensa.state.TestState.Disabled as DisabledState

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

val junit6Descriptor = FrameworkDescriptor(
    initialStateFor = { md ->
        when {
            md.hasAnnotation<Disabled>() -> DisabledState
            md is Method && md.declaringClass.hasAnnotation<Disabled>() -> DisabledState
            else -> NotExecuted
        }
    },
    displayNameFor = { md -> md.findAnnotation<DisplayName>()?.value },
    findTestMethods = { cs -> cs.findTestMethods { it.hasAnnotation<Test>() || it.hasAnnotation<ParameterizedTest>() } },
    isJavaClassTest = isJavaClassTest,
    isJavaInterfaceTest = isJavaInterfaceTest,
    isKotlinTest = isKotlinTest,
)