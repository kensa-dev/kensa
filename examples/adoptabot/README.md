# Adoptabot - Kensa BDD Testing Example

## Overview

Adoptabot is a simple example project that demonstrates how to use Kensa for Behavior-Driven Development (BDD) testing. It implements a basic robot adoption service with HTTP endpoints for checking available robots and adopting them.

## Project Structure

- **Main Components**:
  - `Robot` - A data class representing a robot with an ID, name, and adoption status
  - `AdoptionStatus` - An enum representing the status of a robot (Available or Adopted)
  - `adoptionService()` - A function that creates an HTTP handler with endpoints for the robot adoption service

- **API Endpoints**:
  - `GET /robots` - Returns a list of available robots
  - `PATCH /adopt/{id}` - Adopts a robot by changing its status to Adopted

## BDD Testing with Kensa

This project showcases several key features of Kensa for BDD testing:

### 1. Test Structure

Tests follow the Given-When-Then pattern:
- **Given** - Set up the test context (e.g., create available robots)
- **When** - Perform the action under test (e.g., request to adopt a robot)
- **Then** - Verify the outcome (e.g., check the response status and body)

### 2. Capturing Interactions

Kensa allows capturing interactions between different components (parties) of the system:
- The `AdoptabotParty` enum defines the parties involved (Client and AdoptionService)
- Interactions are captured using the `interactions.capture()` method
- These interactions are rendered in the test reports

### 3. Custom Rendering

The project demonstrates how to customize the rendering of test results:
- `ResponseRenderer` formats HTTP responses for better readability in test reports
- `prettyPrintJson()` extension function formats JSON for better readability

### 4. Test Environment Setup

The `AdoptabotExtension` class shows how to set up and tear down the test environment:
- Starts an HTTP server for the adoption service before tests
- Stops the server after tests
- Configures Kensa with custom renderers

## Running the Tests

To run the tests, execute:

```bash
./gradlew :adoptabot:test
```
The test output will be available in the project [build folder](build/kensa-output/index.html)`build/kensa-output/index.html`


## Learning from this Example

This example demonstrates:
1. How to structure BDD tests with Kensa
2. How to capture and render interactions between system components
3. How to set up and tear down the test environment
4. How to customize the rendering of test results

By studying this example, you can learn how to apply BDD testing principles to your own projects using Kensa.