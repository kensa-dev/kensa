package dev.kensa.example.adoptabot

import dev.kensa.example.adoptabot.AdoptionStatus.Adopted
import dev.kensa.example.adoptabot.AdoptionStatus.Available

/**
 * The shelters seeded into the service for every test. Shared by the fixtures and the
 * scenario seeding so derived values (ids, names) line up across the whole suite.
 */
val shelterCatalogue: List<Shelter> = listOf(
    Shelter("foundry", "Old Foundry", "Sheffield"),
    Shelter("works", "Steamworks", "Birmingham"),
)

/**
 * The robots seeded into the service for every test.
 */
val robotCatalogue: List<Robot> = listOf(
    Robot("1", "Bolt", Available, "foundry"),
    Robot("2", "Chip", Available, "foundry"),
    Robot("3", "Sparks", Adopted, "works"),
    Robot("4", "Buzz", Adopted, "works"),
    Robot("5", "Gizmo", Available, "works"),
    Robot("6", "Cogs", Available, "foundry"),
)

val availableRobots: List<Robot> = robotCatalogue.filter { it.status == Available }
val adoptedRobots: List<Robot> = robotCatalogue.filter { it.status == Adopted }

fun robotNamed(name: String): Robot = robotCatalogue.first { it.name == name }
fun shelterNamed(name: String): Shelter = shelterCatalogue.first { it.name == name }
