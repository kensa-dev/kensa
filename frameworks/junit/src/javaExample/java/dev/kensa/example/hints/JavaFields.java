package dev.kensa.example.hints;

public class JavaFields {
    public static final JavaFieldWithPathHint<String> aString = new JavaFieldWithPathHint<>("/path/To/String/Field");
    public static final JavaFieldWithPathHint<Integer> anInteger = new JavaFieldWithPathHint<>("/path/To/Integer/Field");
}