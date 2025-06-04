package dev.kensa.parse

import dev.kensa.sentence.Sentence

data class Location(val lineNumber: Int, val linePosition: Int)

sealed class Event {

    object EnterMethod : Event()
    object ExitMethod : Event()
    object EnterBlock : Event()
    object ExitBlock : Event()
    object ExitStatement : Event()
    object ExitMethodInvocation : Event()
    object ExitExpression : Event()
    object EnterLambda : Event()
    object ExitLambda : Event()

    class MultilineString(val value: String) : Event()

    object Terminal : Event()
    object EnterTypeArguments : Event()
    object ExitTypeArguments : Event()
}

sealed class LocatedEvent(val location: Location) : Event() {

    class EnterStatement(location: Location) : LocatedEvent(location)
    class EnterMethodInvocation(location: Location) : LocatedEvent(location)
    class EnterExpression(location: Location) : LocatedEvent(location)

    class ScenarioExpression(location: Location, val name: String, val call: String) : LocatedEvent(location)
    class FixturesExpression(location: Location, val name: String) : LocatedEvent(location)
    class ChainedCallExpression(location: Location, val type: Type, val chain: String) : LocatedEvent(location) {
        enum class Type {
            Method, Field, Parameter
        }
    }
    class MethodName(location: Location, val name: String, val emphasis: EmphasisDescriptor = EmphasisDescriptor.Default) : LocatedEvent(location)

    class Operator(location: Location, val text: String) : LocatedEvent(location)
    class Parameter(location: Location, val name: String) : LocatedEvent(location)
    class Method(location: Location, val name: String) : LocatedEvent(location)
    class Field(location: Location, val name: String) : LocatedEvent(location)
    class Nested(location: Location, val name: String, val sentences: List<Sentence>) : LocatedEvent(location)
    class Identifier(location: Location, val name: String, val emphasis: EmphasisDescriptor = EmphasisDescriptor.Default) : LocatedEvent(location)

    class NumberLiteral(location: Location, val value: String) : LocatedEvent(location)
    class BooleanLiteral(location: Location, val value: String) : LocatedEvent(location)
    class CharacterLiteral(location: Location, val value: String) : LocatedEvent(location)
    class StringLiteral(location: Location, val value: String) : LocatedEvent(location)
    class NullLiteral(location: Location) : LocatedEvent(location)
}