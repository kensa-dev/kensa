package dev.kensa.state

sealed interface SeamDefinition {
    val id: String
    val name: String
    val owner: Party
    val correlationFixtures: List<String>
}

data class Inbound(
    override val id: String,
    override val name: String,
    override val owner: Party,
    override val correlationFixtures: List<String> = emptyList(),
) : SeamDefinition

data class Outbound(
    override val id: String,
    override val name: String,
    override val owner: Party,
    override val correlationFixtures: List<String> = emptyList(),
) : SeamDefinition
