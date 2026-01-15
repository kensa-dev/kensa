package dev.kensa.example.hints

object KotlinFields {
    val aString = KotlinFieldWithPathHint<String>("/path/To/String/Field")
    val anInteger = KotlinFieldWithPathHint<Int>("/path/To/Integer/Field")

}