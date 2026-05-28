package dev.kensa.parse

import dev.kensa.parse.java.Java20Parser
import dev.kensa.parse.java.KensaJavaLexer
import dev.kensa.parse.kotlin.KensaKotlinLexer
import dev.kensa.parse.kotlin.KotlinParser
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KensaParseTreeTest {

    @Nested
    inner class KotlinMethodDeclarationContextTest {

        @Test
        fun `captures name, parameters and line range`() {
            val context = KotlinMethodDeclarationContext(kotlinFunctionDeclaration(KOTLIN_SAMPLE))

            context.name shouldBe "a sample test"
            context.parameterNamesAndTypes shouldBe listOf("name" to "String", "age" to "Int")
            context.startLine shouldBe 2
            context.endLine shouldBe 4
        }

        @Test
        fun `detaches the body from its parent chain`() {
            val context = KotlinMethodDeclarationContext(kotlinFunctionDeclaration(KOTLIN_SAMPLE))

            val body = context.body.shouldBeInstanceOf<ParserRuleContext>()
            body.parent shouldBe null
        }
    }

    @Nested
    inner class JavaMethodDeclarationContextTest {

        @Test
        fun `captures name, parameters and line range`() {
            val context = JavaMethodDeclarationContext(javaMethodDeclaration(JAVA_SAMPLE))

            context.name shouldBe "aSampleTest"
            context.parameterNamesAndTypes shouldBe listOf("name" to "String", "age" to "int")
            context.startLine shouldBe 2
            context.endLine shouldBe 4
        }

        @Test
        fun `detaches the body from its parent chain`() {
            val context = JavaMethodDeclarationContext(javaMethodDeclaration(JAVA_SAMPLE))

            val body = context.body.shouldBeInstanceOf<ParserRuleContext>()
            body.parent shouldBe null
        }
    }

    @Nested
    inner class JavaInterfaceDeclarationContextTest {

        @Test
        fun `captures name, parameters and line range`() {
            val context = JavaInterfaceDeclarationContext(javaInterfaceMethodDeclaration(JAVA_INTERFACE_SAMPLE))

            context.name shouldBe "aSampleTest"
            context.parameterNamesAndTypes shouldBe listOf("name" to "String")
            context.startLine shouldBe 2
            context.endLine shouldBe 4
        }

        @Test
        fun `detaches the body from its parent chain`() {
            val context = JavaInterfaceDeclarationContext(javaInterfaceMethodDeclaration(JAVA_INTERFACE_SAMPLE))

            val body = context.body.shouldBeInstanceOf<ParserRuleContext>()
            body.parent shouldBe null
        }
    }
}

private val KOTLIN_SAMPLE = """
    class SampleKotlinTest {
        fun `a sample test`(name: String, age: Int?) {
            val x = name
        }
    }
""".trimIndent()

private val JAVA_SAMPLE = """
    class SampleJavaTest {
        void aSampleTest(String name, int age) {
            int x = age;
        }
    }
""".trimIndent()

private val JAVA_INTERFACE_SAMPLE = """
    interface SampleJavaInterfaceTest {
        default void aSampleTest(String name) {
            String x = name;
        }
    }
""".trimIndent()

private fun kotlinFunctionDeclaration(source: String): KotlinParser.FunctionDeclarationContext {
    val kotlinFile = KotlinParser(CommonTokenStream(KensaKotlinLexer(CharStreams.fromString(source)))).kotlinFile()

    val declarations = mutableListOf<KotlinParser.FunctionDeclarationContext>()
    fun collect(ctx: ParserRuleContext) {
        ctx.children?.forEach { child ->
            when (child) {
                is KotlinParser.FunctionDeclarationContext -> declarations.add(child)
                is ParserRuleContext -> collect(child)
            }
        }
    }
    collect(kotlinFile)

    return declarations.first()
}

private fun javaMethodDeclaration(source: String): Java20Parser.MethodDeclarationContext =
    javaCompilationUnit(source)
        .topLevelClassOrInterfaceDeclaration()
        .mapNotNull { it.classDeclaration() }
        .first()
        .normalClassDeclaration()
        .classBody()
        .classBodyDeclaration()
        .mapNotNull { it.classMemberDeclaration()?.methodDeclaration() }
        .first()

private fun javaInterfaceMethodDeclaration(source: String): Java20Parser.InterfaceMethodDeclarationContext =
    javaCompilationUnit(source)
        .topLevelClassOrInterfaceDeclaration()
        .mapNotNull { it.interfaceDeclaration() }
        .first()
        .normalInterfaceDeclaration()
        .interfaceBody()
        .interfaceMemberDeclaration()
        .mapNotNull { it.interfaceMethodDeclaration() }
        .first()

private fun javaCompilationUnit(source: String): Java20Parser.OrdinaryCompilationUnitContext =
    Java20Parser(CommonTokenStream(KensaJavaLexer(CharStreams.fromString(source)))).compilationUnit().ordinaryCompilationUnit()
