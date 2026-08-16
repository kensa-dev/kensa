package dev.kensa.parse.kotlin

/**
 * Extracts the annotation names applied to a Kotlin function declaration.
 *
 * This is part of the framework-integration SPI: a [dev.kensa.context.FrameworkDescriptor] supplies an
 * `isKotlinTest` predicate, and adapters use this to decide whether a parsed function carries their
 * framework's test annotation.
 */
@dev.kensa.KensaInternalApi
fun KotlinParser.FunctionDeclarationContext.findAnnotationNames(): List<String> {
    val functionAnnotations = modifiers().flatMap { it.annotation() }
    val statementAnnotations = parent?.parent?.takeIf { it is KotlinParser.StatementContext }?.let { (it as KotlinParser.StatementContext).annotation() }.orEmpty()

    val annotationContexts = functionAnnotations + statementAnnotations

    return annotationContexts.mapNotNull {
        val namedWithConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.constructorInvocation()?.userType()?.text
        val namedWithoutConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.userType()?.text

        namedWithConstructorInvocation ?: namedWithoutConstructorInvocation
    }
}
