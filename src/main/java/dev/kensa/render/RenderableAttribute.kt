package dev.kensa.render

interface RenderableAttribute<T, RA> {
    @JvmDefault
    fun showOnSequenceDiagram() : Boolean = false
    fun name(): String
    fun renderableFrom(value: T): RA
}