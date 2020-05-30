package dev.kensa.render

interface RendererWithAttributes<T> : Renderer<T> {
    fun attributes(): Set<RenderableAttribute<T, *>> = emptySet()
}