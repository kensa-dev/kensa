package dev.kensa.compile

import org.jetbrains.kotlin.backend.common.extensions.DeclarationFinder
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.INFO
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.LOGGING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
class KensaIrGenerationExtension(private val messageCollector: MessageCollector, private val debugEnabled: Boolean) : IrGenerationExtension {

    private val expandableSentenceFqName = FqName("dev.kensa.ExpandableSentence")
    private val renderedValueFqName = FqName("dev.kensa.RenderedValue")
    private val expandableRenderedValueFqName = FqName("dev.kensa.ExpandableRenderedValue")
    private val fixtureFqName = FqName("dev.kensa.Fixture")
    private val fixturePackageFqName = "dev.kensa.fixture"


    private val hooksClassId = ClassId.topLevel(FqName("dev.kensa.runtime.CompilerPluginHookFunctions"))

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val finder = pluginContext.finderForBuiltins()
        val hookClass = finder.findClass(hooksClassId) ?: return
        val expandableSentenceHookFn = hookClass.functions.firstOrNull { it.owner.name.asString() == "onEnterExpandableSentence" } ?: return
        val renderedValueHookFn = hookClass.functions.firstOrNull { it.owner.name.asString() == "onExitRenderedValue" } ?: return

        // Get the symbol for kotlin.arrayOf() so we can build the parameter types and argument values arrays
        val arrayOf = finder.arrayOf()
        val factoryFixtureSymbol = finder.findFunctions(CallableId(FqName(fixturePackageFqName), Name.identifier("factoryFixture"))).singleOrNull()
        var expandableSentenceCount = 0
        var renderedValueCount = 0
        var fixtureFactoryCount = 0

        moduleFragment.files.forEach { file ->
            logDebug("Processing file: ${file.name}")
            file.declarations.forEach { decl ->
                when (decl) {
                    is IrClass -> decl.declarations.filterIsInstance<IrSimpleFunction>().forEach { fn ->
                        logDebug("Processing class: ${decl.name}")
                        if (injectForExpandableSentenceIfAnnotated(fn, hookClass.owner, expandableSentenceHookFn.owner, pluginContext, arrayOf)) {
                            expandableSentenceCount++
                        }
                        if (injectForRenderedValueIfAnnotated(fn, hookClass.owner, renderedValueHookFn.owner, pluginContext, arrayOf)) {
                            renderedValueCount++
                        }
                        if (factoryFixtureSymbol != null && rewriteFixtureFactoryIfAnnotated(fn, pluginContext, factoryFixtureSymbol)) {
                            fixtureFactoryCount++
                        }
                    }

                    is IrSimpleFunction -> {
                        logDebug("Processing top-level function: ${decl.name}")
                        if (injectForExpandableSentenceIfAnnotated(decl, hookClass.owner, expandableSentenceHookFn.owner, pluginContext, arrayOf)) {
                            expandableSentenceCount++
                        }
                        if (injectForRenderedValueIfAnnotated(decl, hookClass.owner, renderedValueHookFn.owner, pluginContext, arrayOf)) {
                            renderedValueCount++
                        }
                        if (factoryFixtureSymbol != null && rewriteFixtureFactoryIfAnnotated(decl, pluginContext, factoryFixtureSymbol)) {
                            fixtureFactoryCount++
                        }
                    }
                }
            }
        }

        logInfo("Kensa IR generation completed.")
        logInfo(" - Processed $expandableSentenceCount functions with @ExpandableSentence annotation")
        logInfo(" - Processed $renderedValueCount functions with @RenderedValue/@ExpandableRenderedValue annotation")
        logInfo(" - Processed $fixtureFactoryCount functions with @Fixture annotation")
    }

    /**
     * Rewrites the no-name `fixture { }` call inside a `@`[dev.kensa.Fixture]`("key")` factory function to
     * `factoryFixture("key", <factory params>) { }`, injecting the annotation key and the function's own value
     * parameters as the identity discriminator. The un-rewritten no-name overload fails loud at runtime, so a
     * function reaching that error means the plugin did not instrument its source set.
     */
    private fun rewriteFixtureFactoryIfAnnotated(
        fn: IrSimpleFunction,
        pluginContext: IrPluginContext,
        factoryFixtureSymbol: IrSimpleFunctionSymbol
    ): Boolean {
        val annotation = fn.annotations.firstOrNull { it.type.classOrNull?.owner?.fqNameWhenAvailable == fixtureFqName } ?: return false
        val key = (annotation.arguments.firstOrNull() as? IrConst)?.value as? String ?: return false

        val identityParams = fn.parameters.filter { it.kind == IrParameterKind.Regular }
        val anyN = pluginContext.irBuiltIns.anyNType
        val builder = DeclarationIrBuilder(pluginContext, fn.symbol, fn.startOffset, fn.endOffset)

        var rewrote = false
        fn.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                val callee = expression.symbol.owner
                val isNoNameFixture = callee.name.asString() == "fixture" &&
                        callee.getPackageFragment().packageFqName.asString() == fixturePackageFqName &&
                        callee.parentClassOrNull == null &&
                        callee.parameters.count { it.kind == IrParameterKind.Regular } == 1

                if (!isNoNameFixture) return super.visitCall(expression)

                rewrote = true
                val factoryArgIndex = callee.parameters.indexOfFirst { it.kind == IrParameterKind.Regular }
                val factoryArg = expression.arguments[factoryArgIndex]
                    ?: return super.visitCall(expression)

                return builder.irCall(factoryFixtureSymbol).apply {
                    typeArguments[0] = expression.typeArguments[0]
                    arguments[0] = builder.irString(key)
                    arguments[1] = builder.irVararg(anyN, identityParams.map { vp ->
                        val arg = builder.irGet(vp)
                        if (vp.type.isPrimitiveType()) builder.irAs(arg, anyN) else arg
                    })
                    arguments[2] = factoryArg
                }
            }
        })

        return rewrote
    }

    private fun injectForRenderedValueIfAnnotated(
        fn: IrSimpleFunction,
        hookClassOwner: IrClass,
        hookFnOwner: IrSimpleFunction,
        pluginContext: IrPluginContext,
        arrayOf: IrSimpleFunctionSymbol
    ): Boolean {
        val hasAnnotation = fn.annotations.hasAnnotation(renderedValueFqName) || fn.annotations.hasAnnotation(expandableRenderedValueFqName)
        if (!hasAnnotation) return false

        val ctx = prepareInjection(fn, pluginContext, arrayOf) ?: return false

        val tempVar = ctx.builder.scope.createTemporaryVariableDeclaration(
            startOffset = ctx.builder.startOffset,
            endOffset = ctx.builder.endOffset,
            nameHint = "result",
            irType = fn.returnType
        )

        val originalReturnExpression: IrExpression = when (val lastStatement = ctx.blockBody.statements.lastOrNull()) {
            is IrReturn -> {
                ctx.blockBody.statements.removeAt(ctx.blockBody.statements.size - 1)
                lastStatement.value
            }

            else -> if (ctx.blockBody.statements.isNotEmpty()) {
                ctx.blockBody.statements.removeAt(ctx.blockBody.statements.size - 1) as? IrExpression ?: ctx.builder.irNull()
            } else ctx.builder.irNull()
        }

        tempVar.initializer = originalReturnExpression
        ctx.blockBody.statements.add(tempVar)
        ctx.blockBody.statements.add(ctx.buildHookCall(hookClassOwner, hookFnOwner, ctx.builder.irGet(tempVar)))
        ctx.blockBody.statements.add(ctx.builder.irReturn(ctx.builder.irGet(tempVar)))

        return true
    }

    private fun injectForExpandableSentenceIfAnnotated(
        fn: IrSimpleFunction,
        hookClassOwner: IrClass,
        hookFnOwner: IrSimpleFunction,
        pluginContext: IrPluginContext,
        arrayOf: IrSimpleFunctionSymbol
    ): Boolean {
        val hasAnnotation = fn.annotations.hasAnnotation(expandableSentenceFqName)
        if (!hasAnnotation) return false

        val ctx = prepareInjection(fn, pluginContext, arrayOf) ?: return false

        val argsArray = ctx.builder.buildArgsArray(arrayOf, ctx.allParams)
        ctx.blockBody.statements.add(0, ctx.buildHookCall(hookClassOwner, hookFnOwner, argsArray))

        return true
    }

    private class InjectionContext(
        val blockBody: IrBlockBody,
        val builder: DeclarationIrBuilder,
        val simpleName: IrExpression,
        val allParams: List<IrValueParameter>
    ) {
        fun buildHookCall(hookClassOwner: IrClass, hookFnOwner: IrSimpleFunction, lastArg: IrExpression): IrCall =
            builder.irCall(hookFnOwner.symbol).apply {
                dispatchReceiver = builder.irGetObject(hookClassOwner.symbol)
                arguments[1] = simpleName
                arguments[2] = lastArg
            }
    }

    private fun prepareInjection(
        fn: IrSimpleFunction,
        pluginContext: IrPluginContext,
        arrayOf: IrSimpleFunctionSymbol
    ): InjectionContext? {
        val body = fn.body ?: return null

        val blockBody: IrBlockBody = when (body) {
            is IrBlockBody -> body
            is IrExpressionBody -> {
                val newBody = pluginContext.irFactory.createBlockBody(body.startOffset, body.endOffset)
                newBody.statements += body.expression
                fn.body = newBody
                newBody
            }

            else -> return null
        }

        val builder = DeclarationIrBuilder(pluginContext, fn.symbol, fn.startOffset, fn.endOffset)

        val contextParams = fn.parameters.filter { it.kind == IrParameterKind.Context }
        val extensionParam = fn.parameters.firstOrNull { it.kind == IrParameterKind.ExtensionReceiver }
        val valueParameters = fn.parameters.filter { it.kind == IrParameterKind.Regular }
        val allParams: List<IrValueParameter> = buildList {
            addAll(contextParams)
            extensionParam?.let { add(it) }
            addAll(valueParameters)
        }

        val simpleName = builder.irString(fn.name.asString())

        return InjectionContext(blockBody, builder, simpleName, allParams)
    }

    // Gets the `kotlin.arrayOf` function symbol so we can build arrays with it
    private fun DeclarationFinder.arrayOf() =
        findFunctions(CallableId(FqName("kotlin"), Name.identifier("arrayOf")))
            .single { fn ->
                fn.owner.typeParameters.size == 1 &&
                        fn.owner.parameters.size == 1 &&
                        fn.owner.parameters[0].varargElementType != null
            }

    // Builds the call to build the array of arguments to pass to the hook function
    private fun IrBuilderWithScope.buildArgsArray(arrayOf: IrSimpleFunctionSymbol, valueParameters: List<IrValueParameter>): IrCall {
        val anyN = context.irBuiltIns.anyNType
        val argElements = valueParameters.map { vp ->
            val arg = irGet(vp)
            if (vp.type.isPrimitiveType()) {
                irAs(arg, anyN)
            } else {
                arg
            }
        }

        return irCall(arrayOf).apply {
            typeArguments[0] = anyN
            arguments[0] = irVararg(anyN, argElements)
        }
    }

    private fun logInfo(message: String) {
        messageCollector.report(INFO, "[Kensa] $message")
    }

    private fun logDebug(message: String) {
        if (debugEnabled) {
            messageCollector.report(LOGGING, "[Kensa] DEBUG: $message")
        }
    }
}