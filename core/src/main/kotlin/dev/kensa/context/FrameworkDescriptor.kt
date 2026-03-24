package dev.kensa.context

import dev.kensa.parse.java.Java20Parser
import dev.kensa.parse.kotlin.KotlinParser
import dev.kensa.state.TestState
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

data class FrameworkDescriptor(
    val initialStateFor: (AnnotatedElement) -> TestState,
    val displayNameFor: (AnnotatedElement) -> String?,
    val findTestMethods: (Class<*>) -> Set<Method>,
    val isJavaClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean,
    val isJavaInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean,
    val isKotlinTest: (KotlinParser.FunctionDeclarationContext) -> Boolean,
)
