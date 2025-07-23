---
sidebar_position: 1.5
---

# Important Deprecation Notice

## Deprecated Interfaces

In the latest version of Kensa, the following interfaces have been deprecated:

- `GivensBuilder` - This has been replaced with `Action<GivensContext>`
- `ActionUnderTest` - This has been replaced with `Action<ActionContext>`
- `StateExtractor` - This has been replaced with `StateCollector<CollectorContext>`

The new functional interfaces simplify implementation by using a single parameter, which encapsulates all necessary data. 
This allows for cleaner, more readable code by enabling destructuring directly in the implementation.
It reduces parameter noise, improves maintainability, and aligns with functional programming best practices.


## Updating Your Code

If you're using these deprecated interfaces in your code, you should update to the new interfaces. 

### Example: Replacing GivensBuilder

Instead of:

```kotlin
private fun prepareForTest() = GivensBuilder { givens: Givens ->
    givens.put("key", "value")
}
```

Use:

```kotlin
private fun prepareForTest() = Action<GivensContext> { (fixtures) ->
    // Do something with your fixtures
    service.call(fixtures[MyFixture])
}
```

### Example: Replacing ActionUnderTest

Instead of:

```kotlin
private fun someAction() = ActionUnderTest { givens, interactions ->
    // Your action code here
}
```

Use:

```kotlin
private fun someAction() = Action<ActionContext> { (fixtures, interactions, output) ->
}
```

Please refer to the API documentation for more details on these new interfaces.