package dev.kensa.spring

import dev.kensa.PackageDisplay
import dev.kensa.Tab
import dev.kensa.state.SetupStrategy
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URL
import java.nio.file.Path

@ConfigurationProperties(prefix = "kensa")
data class KensaSpringProperties(
    val outputDir: Path? = null,
    val outputEnabled: Boolean? = null,
    val titleText: String? = null,
    val issueTrackerUrl: URL? = null,
    val tabSize: Int? = null,
    val autoOpenTab: Tab? = null,
    val autoExpandNotes: Boolean? = null,
    val setupStrategy: SetupStrategy? = null,
    val flattenOutputPackages: Boolean? = null,
    val packageDisplay: PackageDisplay? = null,
    val packageDisplayRoot: String? = null,
)
