---
sidebar_position: 1
---

# Introduction to BDD with Kensa

## What is Behavior-Driven Development (BDD)?

Behavior-Driven Development (BDD) is an agile software development methodology that encourages collaboration between developers, QA, and non-technical or business participants in a software project. It extends Test-Driven Development (TDD) by writing test cases in a natural language that non-programmers can read.

BDD focuses on:

- **Defining behavior in a shared language** - Creating a ubiquitous language that all team members can understand
- **Documenting examples** - Using concrete examples to illustrate the expected behavior
- **Automating validation** - Converting examples into automated tests

The core of BDD is the ability to express tests in a language that both technical and non-technical stakeholders can understand, while still being executable as automated tests.

## Why BDD is Beneficial

BDD offers numerous advantages for software development teams:

1. **Improved Communication** - BDD bridges the gap between technical and non-technical team members by using a common language to describe system behavior.

2. **Living Documentation** - Tests serve as documentation that is always up-to-date because it's executable and verified with each build.

3. **Focus on User Value** - By describing behavior from the user's perspective, teams stay focused on delivering features that provide real value.

4. **Reduced Rework** - Clear specifications from the beginning help avoid misunderstandings that lead to rework.

5. **Higher Quality Code** - Writing tests before implementation leads to better design and more maintainable code.

6. **Faster Feedback** - Automated tests provide immediate feedback on whether the system behaves as expected.

## Introducing Kensa

Kensa is a modern BDD testing framework for Kotlin and Java that simplifies behavior-driven development. Unlike traditional BDD frameworks that require separate text files for specifications, Kensa allows you to write your tests directly in your code using a natural, fluent syntax.

### Key Features of Kensa

- **Native Language Support** - Write tests in Kotlin or Java without the need for external DSL files
- **Expressive Syntax** - Use a fluent, readable syntax that follows the Given-When-Then pattern
- **Rich HTML Reports** - Generate comprehensive reports directly from your test code
- **Sequence Diagrams** - Automatically generate sequence diagrams to visualize interactions
- **Framework Integration** - Seamlessly integrate with JUnit5. TestNG and Kotest coming soon!
- **Flexible Assertions** - Use your preferred assertion library (Hamcrest, HamKrest, Kotest, AssertJ)
- **Variable Tracking** - Capture and display relevant variables in your reports

### How Kensa Differs from Traditional BDD Frameworks

Traditional BDD frameworks like Cucumber require you to:
1. Write feature files in Gherkin syntax
2. Implement step definitions that map to the Gherkin statements
3. Maintain the mapping between the two

Kensa simplifies this process by:
1. Writing tests directly in your code using a natural, fluent syntax
2. Automatically generating documentation from your code
3. Eliminating the need to maintain separate feature files

This approach reduces overhead while still providing the benefits of BDD, making it easier to adopt and maintain over time.

In the following sections, we'll show you how to get started with Kensa and demonstrate its capabilities through practical examples.
