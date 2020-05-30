package dev.kensa.render

interface RenderableAttribute<T, RA> {
    fun name(): String
    fun renderableFrom(value: T): RA
}