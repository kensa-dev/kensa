package dev.kensa.adoptabot

import dev.kensa.state.Party

/**
 * Defines the parties involved in the Adoptabot BDD tests.
 * 
 * In BDD testing with Kensa, parties represent the different components or actors
 * in the system that interact with each other. These interactions are captured
 * and rendered in the test reports.
 * 
 * The Adoptabot system has two parties:
 * - Client: The client making requests to the adoption service
 * - AdoptionService: The service that handles robot adoption requests
 */
enum class AdoptabotParty : Party {
    /**
     * Represents the client making requests to the adoption service.
     */
    Client,

    /**
     * Represents the adoption service that handles robot adoption requests.
     */
    AdoptionService;

    /**
     * Returns the string representation of the party.
     * 
     * @return The name of the party
     */
    override fun asString(): String = name
}
