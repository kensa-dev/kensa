package dev.kensa.hints

object KotlinFields {
    val aString = KotlinFieldWithPathHint<String>("/path/To/String/Field")
    val anInteger = KotlinFieldWithPathHint<Int>("/path/To/Integer/Field")

}