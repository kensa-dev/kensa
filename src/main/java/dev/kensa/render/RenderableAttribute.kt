package dev.kensa.render

@JvmDefaultWithCompatibility
interface RenderableAttribute<T, RA> {
    fun showOnSequenceDiagram(): Boolean = false
    fun name(): String
    fun renderableFrom(value: T): RA
}