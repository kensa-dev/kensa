package dev.kensa.render.diagram.directive

enum class ArrowStyle(val value: String) {
    UmlAsynchronous1("->>"),
    UmlAsynchronous2("-//"),
    UmlAsynchronousDelete("->>x"),
    UmlResponse("-->>"),
    UmlSynchronous("->"),
    UmlSynchronousDelete("->x"),
    ;
}