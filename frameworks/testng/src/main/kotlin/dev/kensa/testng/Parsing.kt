package dev.kensa.testng

import dev.kensa.context.FrameworkDescriptor
import dev.kensa.parse.java.Java20Parser
import dev.kensa.parse.kotlin.KotlinParser
import dev.kensa.parse.kotlin.KotlinParserDelegate.Companion.findAnnotationNames
import dev.kensa.state.TestState.NotExecuted
import dev.kensa.util.findAnnotation
import dev.kensa.util.findTestMethods
import dev.kensa.util.hasAnnotation
import org.testng.annotations.Test
import dev.kensa.state.TestState.Disabled as DisabledState

private val testAnnotationNames = listOf("Test", "org.testng.annotations.Test")

val isJavaClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean = { context ->
    context.methodModifier().any { it.annotation().hasTestAnnotation() }
}

val isJavaInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean = { context ->
    context.interfaceMethodModifier().any { it.annotation().hasTestAnnotation() }
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

val testNgDescriptor = FrameworkDescriptor(
    initialStateFor = { md ->
        val annotation = md.findAnnotation<Test>()
        if (annotation != null && !annotation.enabled) DisabledState else NotExecuted
    },
    displayNameFor = { md -> md.findAnnotation<Test>()?.description?.takeIf { it.isNotEmpty() } },
    findTestMethods = { cs -> cs.findTestMethods { it.hasAnnotation<Test>() } },
    isJavaClassTest = isJavaClassTest,
    isJavaInterfaceTest = isJavaInterfaceTest,
    isKotlinTest = isKotlinTest,
)
