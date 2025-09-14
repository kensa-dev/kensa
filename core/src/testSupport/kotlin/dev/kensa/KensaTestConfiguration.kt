package dev.kensa

object ThreadLocalKensaConfigurationProvider : KensaConfigurationProvider {
    private val holder = ThreadLocal<Configuration>()

    override fun invoke(): Configuration = holder.get()

    fun set(configuration: Configuration) {
        holder.set(configuration)
    }

    fun remove() {
        holder.remove()
    }
}

fun testConfiguration(block: Configuration.() -> Unit) = ThreadLocalKensaConfigurationProvider().apply(block)
