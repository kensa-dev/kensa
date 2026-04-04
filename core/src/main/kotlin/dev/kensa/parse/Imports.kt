package dev.kensa.parse

import dev.kensa.util.toClassOrMaybeNested

class Imports(
    private val imports: Set<Class<*>>,
    private val wildcardPackages: Set<String>,
    private val declaringClass: Class<*>
) {

    operator fun plus(other: Imports): Imports =
        Imports(imports + other.imports, wildcardPackages + other.wildcardPackages, declaringClass)

    fun match(methodParameterTypes: Array<Class<*>>, sourceParameterTypes: List<String>): Boolean {
        if (methodParameterTypes.size != sourceParameterTypes.size) return false

        return methodParameterTypes.zip(sourceParameterTypes).all { (parameterType, sourceType) ->
            val sourceNoGenerics = sourceType.substringBefore('<')
            val simpleSourceName = sourceNoGenerics.substringAfterLast('.')

            when {
                parameterType.isFullyQualifiedMatch(sourceNoGenerics) -> true

                parameterType.isEquivalentToKotlin(simpleSourceName) -> true

                parameterType.simpleName == simpleSourceName && parameterType.isInContext() -> true

                isImportedNestedMatch(parameterType, sourceNoGenerics) -> true

                isFunctionTypeMatch(parameterType, sourceType) -> true

                else -> false
            }
        }
    }

    private fun Class<*>.isInContext(): Boolean =
        this in imports ||
            isNestedOfImport() ||
            safePackageName == this@Imports.declaringClass.safePackageName ||
            isInDefaultPackage() ||
            isInWildcardPackage()

    private fun Class<*>.isNestedOfImport(): Boolean {
        // Use getDeclaringClass() to avoid Kotlin's non-null intrinsic check on the property
        val parent = declaringClass ?: return false
        return parent in imports || parent.isNestedOfImport()
    }

    private fun Class<*>.isFullyQualifiedMatch(sourceName: String): Boolean {
        if (name == sourceName) return true

        var attempt = sourceName
        while (attempt.contains('.')) {
            attempt = attempt.replaceLast('.', '$')
            if (name == attempt) return true
        }
        return false
    }

    private fun isImportedNestedMatch(methodType: Class<*>, sourceName: String): Boolean {
        val topLevelName = sourceName.substringBefore('.')
        if (topLevelName == sourceName) return false

        val importedParent = imports.find { it.simpleName == topLevelName } ?: return false

        val nestedPath = sourceName.substringAfter('.').replace('.', '$')
        return methodType.name == "${importedParent.name}$$nestedPath"
    }

    private fun isFunctionTypeMatch(methodType: Class<*>, sourceType: String): Boolean {
        val paramCount = parseFunctionTypeParamCount(sourceType) ?: return false
        return methodType.name == "kotlin.jvm.functions.Function$paramCount"
    }

    private fun parseFunctionTypeParamCount(sourceType: String): Int? {
        if (!sourceType.startsWith('(')) return null
        var depth = 0
        var commaCount = 0
        var hasContent = false
        for (c in sourceType) {
            when (c) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) {
                        val rest = sourceType.substringAfter(')').trimStart()
                        return if (rest.startsWith("->")) (if (hasContent) commaCount + 1 else 0) else null
                    }
                }
                ',' -> if (depth == 1) { commaCount++; hasContent = true }
                else -> if (depth == 1 && !c.isWhitespace()) hasContent = true
            }
        }
        return null
    }

    private fun Class<*>.isEquivalentToKotlin(simpleName: String): Boolean =
        when (simpleName) {
            "Int" -> this == Int::class.javaObjectType || this == Int::class.javaPrimitiveType
            "Long" -> this == Long::class.javaObjectType || this == Long::class.javaPrimitiveType
            "Boolean" -> this == Boolean::class.javaObjectType || this == Boolean::class.javaPrimitiveType
            "Double" -> this == Double::class.javaObjectType || this == Double::class.javaPrimitiveType
            "Any" -> this == Any::class.java
            else -> false
        }

    private fun Class<*>.isInWildcardPackage(): Boolean {
        val pkg = safePackageName ?: return false
        return pkg in wildcardPackages
    }

    private fun Class<*>.isInDefaultPackage(): Boolean =
        safePackageName in DEFAULT_PACKAGES || isDefaultMappedCollection()

    private fun Class<*>.isDefaultMappedCollection(): Boolean =
        safePackageName == "java.util" && simpleName in KOTLIN_COLLECTIONS

    private val Class<*>?.safePackageName: String?
        get() = this?.`package`?.name

    private fun String.replaceLast(old: Char, new: Char): String {
        val index = lastIndexOf(old)
        return if (index == -1) this else StringBuilder(this).apply { setCharAt(index, new) }.toString()
    }

    companion object {
        private val KOTLIN_COLLECTIONS = setOf("List", "Map", "Set")
        private val DEFAULT_PACKAGES = setOf(
            "java.lang",
            "kotlin",
            "kotlin.collections",
            "kotlin.ranges",
            "kotlin.sequences",
            "kotlin.text",
            "kotlin.io"
        )

        operator fun invoke(importStrings: List<String>, declaringClass: Class<*>): Imports {
            val specificImports = importStrings.filterNot { it.endsWith(".*") }
                .mapNotNull { it.toClassOrMaybeNested() }
                .toSet()

            val wildcardPackages = importStrings.filter { it.endsWith(".*") }
                .map { it.removeSuffix(".*") }
                .toSet()

            return Imports(specificImports, wildcardPackages, declaringClass)
        }
    }
}