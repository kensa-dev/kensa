package dev.kensa.render

interface Renderer<T> {
    fun render(value: T): String
}