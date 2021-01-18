package dev.kensa.render.diagram.directive

enum class ArrowStyle(val value: String) {
    ArrowCross("->x"),
    ArrowThin("->"),
    ArrowThick("->>"),
    ArrowCircle("->o"),
    ArrowDotted("-->"),
    ArrowDottedCross("-->x"),
    ArrowDottedCircle("-->o")
    ;
}