package dev.kensa.compile

import dev.kensa.compile.KensaCommandLineProcessor.Companion.DEBUG_KEY
import dev.kensa.compile.KensaCommandLineProcessor.Companion.ENABLED_KEY
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.INFO
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey.create

const val PLUGIN_ID = "dev.kensa.compiler-plugin"

@OptIn(ExperimentalCompilerApi::class)
class KensaPluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = PLUGIN_ID
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val enabled = configuration.get(ENABLED_KEY, true)
        val debug = configuration.get(DEBUG_KEY, false)
        val messageCollector = configuration.get(MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        if (enabled) {
            messageCollector.report(INFO, "Kensa compiler plugin enabled (debug: $debug)")

            IrGenerationExtension.registerExtension(KensaIrGenerationExtension(messageCollector, debug))
        } else {
            messageCollector.report(INFO, "Kensa compiler plugin disabled")
        }
    }
}

@OptIn(ExperimentalCompilerApi::class)
class KensaCommandLineProcessor : CommandLineProcessor {
    companion object {

        val ENABLED_KEY = create<Boolean>("enabled")
        val DEBUG_KEY = create<Boolean>("debug")
        
        val ENABLED_OPTION = CliOption(
            optionName = "enabled",
            valueDescription = "<true|false>",
            description = "Enable/disable the Kensa compiler plugin",
            required = false
        )
        
        val DEBUG_OPTION = CliOption(
            optionName = "debug", 
            valueDescription = "<true|false>",
            description = "Enable debug logging for the Kensa compiler plugin",
            required = false
        )
    }

    override val pluginId: String = PLUGIN_ID
    override val pluginOptions: Collection<CliOption> = listOf(ENABLED_OPTION, DEBUG_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option) {
            ENABLED_OPTION -> configuration.put(ENABLED_KEY, value.toBoolean())
            DEBUG_OPTION -> configuration.put(DEBUG_KEY, value.toBoolean())
            else -> throw IllegalArgumentException("Unknown option: ${option.optionName}")
        }
    }
}
