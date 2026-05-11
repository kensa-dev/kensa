package dev.kensa.spring

import dev.kensa.Configuration
import dev.kensa.Kensa
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.core.env.Environment
import org.springframework.test.context.junit.jupiter.SpringExtension

class KensaSpringExtension : BeforeAllCallback {
    override fun beforeAll(ctx: ExtensionContext) {
        val env = SpringExtension.getApplicationContext(ctx).environment
        applyKensaProperties(env)
    }
}

internal fun applyKensaProperties(env: Environment) {
    val props = Binder.get(env)
        .bind("kensa", KensaSpringProperties::class.java)
        .orElse(KensaSpringProperties())
    Kensa.konfigure { applyFrom(props) }
}

internal fun Configuration.applyFrom(p: KensaSpringProperties) {
    p.outputDir?.let { outputDir = it }
    p.outputEnabled?.let { isOutputEnabled = it }
    p.titleText?.let { titleText = it }
    p.issueTrackerUrl?.let { issueTrackerUrl = it }
    p.tabSize?.let { tabSize = it }
    p.autoOpenTab?.let { autoOpenTab = it }
    p.autoExpandNotes?.let { autoExpandNotes = it }
    p.setupStrategy?.let { setupStrategy = it }
    p.flattenOutputPackages?.let { flattenOutputPackages = it }
    p.packageDisplay?.let { packageDisplay = it }
    p.packageDisplayRoot?.let { packageDisplayRoot = it }
}
