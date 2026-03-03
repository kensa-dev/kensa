package dev.kensa.example.hints

class KotlinFieldWithPathHint<T>(val path: String) {
    infix fun of(value: T) = this
}