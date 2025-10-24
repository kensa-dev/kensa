package dev.kensa.compile

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.INFO
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.LOGGING
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
class KensaIrGenerationExtension(private val messageCollector: MessageCollector, private val debugEnabled: Boolean) : IrGenerationExtension {

    private val nestedSentenceFqName = FqName("dev.kensa.NestedSentence")
    private val renderedValueFqName = FqName("dev.kensa.RenderedValue")

    private val hooksClassId = ClassId.topLevel(FqName("dev.kensa.runtime.CompilerPluginHookFunctions"))

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val hookClass = pluginContext.referenceClass(hooksClassId) ?: return
        val nestedSentenceHookFn = hookClass.functions.firstOrNull { it.owner.name.asString() == "onEnterNestedSentence" } ?: return
        val renderedValueHookFn = hookClass.functions.firstOrNull { it.owner.name.asString() == "onExitRenderedValue" } ?: return

        // Get the symbol for kotlin.arrayOf() so we can build the parameter types and argument values arrays
        val arrayOf = pluginContext.arrayOf()
        var nestedSentenceCount = 0
        var renderedValueCount = 0

        moduleFragment.files.forEach { file ->
            logDebug("Processing file: ${file.name}")
            file.declarations.forEach { decl ->
                when (decl) {
                    is IrClass -> decl.declarations.filterIsInstance<IrSimpleFunction>().forEach { fn ->
                        logDebug("Processing class: ${decl.name}")
                        if (injectForNestedSentenceIfAnnotated(fn, file, hookClass.owner, nestedSentenceHookFn.owner, pluginContext, arrayOf)) {
                            nestedSentenceCount++
                        }
                        if (injectForRenderedValueIfAnnotated(fn, file, hookClass.owner, renderedValueHookFn.owner, pluginContext, arrayOf)) {
                            renderedValueCount++
                        }
                    }

                    is IrSimpleFunction -> {
                        logDebug("Processing top-level function: ${decl.name}")
                        if (injectForNestedSentenceIfAnnotated(decl, file, hookClass.owner, nestedSentenceHookFn.owner, pluginContext, arrayOf)) {
                            nestedSentenceCount++
                        }
                        if (injectForRenderedValueIfAnnotated(decl, file, hookClass.owner, renderedValueHookFn.owner, pluginContext, arrayOf)) {
                            renderedValueCount++
                        }
                    }
                }
            }
        }

        logInfo("Kensa IR generation completed.")
        logInfo(" - Processed $nestedSentenceCount functions with @NestedSentence annotation")
        logInfo(" - Processed $renderedValueCount functions with @RenderedValue annotation")
    }

    private fun injectForRenderedValueIfAnnotated(
        fn: IrSimpleFunction,
        file: IrFile,
        hookClassOwner: IrClass,
        hookFnOwner: IrSimpleFunction,
        pluginContext: IrPluginContext,
        arrayOf: IrSimpleFunctionSymbol
    ): Boolean {
        val hasAnnotation = fn.annotations.hasAnnotation(renderedValueFqName)
        if (!hasAnnotation) return false
        val body = fn.body ?: return false

        val blockBody: IrBlockBody = when (body) {
            is IrBlockBody -> body
            is IrExpressionBody -> {
                val factory = pluginContext.irFactory
                val newBody = factory.createBlockBody(body.startOffset, body.endOffset)
                newBody.statements += body.expression
                fn.body = newBody
                newBody
            }

            else -> return false
        }

        val builder = DeclarationIrBuilder(pluginContext, fn.symbol, fn.startOffset, fn.endOffset)
        val ownerExpr = fn.dispatchReceiverParameter?.let { builder.irGet(it) } ?: builder.irNull()

        val valueParameters = fn.parameters.filter { it.kind == IrParameterKind.Regular }

        val ownerFqName = builder.irString(fn.parentClassOrNull?.fqNameWhenAvailable?.asString() ?: "${file.packageFqName.asString()}.${fn.name.asString()}")
        val simpleName = builder.irString(fn.name.asString())
        val paramTypesArray = builder.buildParamTypesArray(arrayOf, valueParameters)

        val tempVar = builder.scope.createTemporaryVariableDeclaration(
            startOffset = builder.startOffset,
            endOffset = builder.endOffset,
            origin = IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
            nameHint = "result",
            irType = fn.returnType
        )

        val lastStatement = blockBody.statements.lastOrNull()
        val originalReturnExpression: IrExpression = when (lastStatement) {
            is IrReturn -> {
                blockBody.statements.removeAt(blockBody.statements.size - 1)
                lastStatement.value
            }

            else -> if (blockBody.statements.isNotEmpty()) {
                blockBody.statements.removeAt(blockBody.statements.size - 1) as? IrExpression ?: builder.irNull()
            } else builder.irNull()
        }

        tempVar.initializer = originalReturnExpression
        blockBody.statements.add(tempVar)

        val call = builder.irCall(hookFnOwner.symbol).apply {
            // This goes into arguments[0]
            dispatchReceiver = builder.irGetObject(hookClassOwner.symbol)

            arguments[1] = ownerExpr
            arguments[2] = ownerFqName
            arguments[3] = simpleName
            arguments[4] = paramTypesArray
            arguments[5] = builder.irGet(tempVar)
        }

        blockBody.statements.add(call)

        blockBody.statements.add(builder.irReturn(builder.irGet(tempVar)))

        return true
    }

    private fun injectForNestedSentenceIfAnnotated(
        fn: IrSimpleFunction,
        file: IrFile,
        hookClassOwner: IrClass,
        hookFnOwner: IrSimpleFunction,
        pluginContext: IrPluginContext,
        arrayOf: IrSimpleFunctionSymbol
    ): Boolean {
        val hasAnnotation = fn.annotations.hasAnnotation(nestedSentenceFqName)
        if (!hasAnnotation) return false
        val body = fn.body ?: return false

        val blockBody: IrBlockBody = when (body) {
            is IrBlockBody -> body
            is IrExpressionBody -> {
                val factory = pluginContext.irFactory
                val newBody = factory.createBlockBody(body.startOffset, body.endOffset)
                newBody.statements += body.expression
                fn.body = newBody
                newBody
            }

            else -> return false
        }

        val builder = DeclarationIrBuilder(pluginContext, fn.symbol, fn.startOffset, fn.endOffset)
        val ownerExpr = fn.dispatchReceiverParameter?.let { builder.irGet(it) } ?: builder.irNull()

        val valueParameters = fn.parameters.filter { it.kind == IrParameterKind.Regular }

        val ownerFqName = builder.irString(fn.parentClassOrNull?.fqNameWhenAvailable?.asString() ?: "${file.packageFqName.asString()}.${fn.name.asString()}")
        val simpleName = builder.irString(fn.name.asString())
        val paramTypesArray = builder.buildParamTypesArray(arrayOf, valueParameters)
        val argsArray = builder.buildArgsArray(arrayOf, valueParameters)

        val call = builder.irCall(hookFnOwner.symbol).apply {
            // This goes into arguments[0]
            dispatchReceiver = builder.irGetObject(hookClassOwner.symbol)

            arguments[1] = ownerExpr
            arguments[2] = ownerFqName
            arguments[3] = simpleName
            arguments[4] = paramTypesArray
            arguments[5] = argsArray
        }

        blockBody.statements.add(0, call)

        return true
    }

    // Gets the `kotlin.arrayOf` function symbol so we can build arrays with it
    private fun IrPluginContext.arrayOf() =
        referenceFunctions(CallableId(FqName("kotlin"), Name.identifier("arrayOf")))
            .single { fn ->
                fn.owner.typeParameters.size == 1 &&
                        fn.owner.parameters.size == 1 &&
                        fn.owner.parameters[0].varargElementType != null
            }

    // Builds the call to build the array of parameter types to pass to the hook function
    private fun IrBuilderWithScope.buildParamTypesArray(arrayOf: IrSimpleFunctionSymbol, valueParameters: List<IrValueParameter>): IrCall {
        val classStarType = context.irBuiltIns.kClassClass.starProjectedType
        val classElements = valueParameters.map { vp ->
            IrClassReferenceImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                type = context.irBuiltIns.kClassClass.typeWith(vp.type),
                symbol = vp.type.classOrNull!!,
                classType = vp.type
            )
        }
        return irCall(arrayOf).apply {
            typeArguments[0] = classStarType
            arguments[0] = irVararg(classStarType, classElements)
        }
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