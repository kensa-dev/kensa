---
sidebar_position: 4
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Captured Outputs

`CapturedOutputs` is a thread-safe store for values produced during the `whenever` step — HTTP responses, returned objects, database query results, etc. Stored values are available in `then` for assertions and appear in the **Captured Outputs** tab of the HTML report.

---

## Typed Keys

The preferred approach is to define a typed key upfront. This gives you compile-time safety on both `put` and `get`.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
// Define once, e.g. as a companion object field or top-level val
val response = capturedOutput<HttpResponse>("response")

class PaymentTest : KensaTest, WithKotest {

    @Test
    fun `payment is accepted`() {
        given { /* ... */ }
        whenever { ctx ->
            ctx.outputs.put(response, paymentGateway.submit(ctx.fixtures[paymentRequest]))
        }
        then({ ctx -> ctx.outputs[response] }) {
            it.statusCode shouldBe 200
        }
    }
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
import static dev.kensa.outputs.CapturedOutputsKt.createCapturedOutput;

static final CapturedOutput<HttpResponse> RESPONSE =
    createCapturedOutput("response", HttpResponse.class);

@Test
void paymentIsAccepted() {
    given(ctx -> { /* ... */ });
    whenever(ctx -> ctx.getOutputs().put(RESPONSE,
        paymentGateway.submit(ctx.getFixtures().get(PAYMENT_REQUEST))));
    then(ctx -> ctx.getOutputs().get(RESPONSE),
        response -> assertThat(response.statusCode()).isEqualTo(200));
}
```

</TabItem>
</Tabs>

---

## String Keys

For quick, untyped access — useful when the value type is obvious from context:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
whenever { ctx ->
    ctx.outputs.put("response", httpClient.post("/payments", body))
}
then({ ctx -> ctx.outputs["response"] as HttpResponse }) {
    it.statusCode shouldBe 200
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
whenever(ctx -> ctx.getOutputs().put("response", httpClient.post("/payments", body)));
then(ctx -> (HttpResponse) ctx.getOutputs().get("response"),
    response -> assertThat(response.statusCode()).isEqualTo(200));
```

</TabItem>
</Tabs>

---

## API Reference

### `capturedOutput<T>()` factory (Kotlin)

```kotlin
inline fun <reified T : Any> capturedOutput(
    key: String,
    highlighted: Boolean = false
): CapturedOutput<T>
```

### `createCapturedOutput()` factory (Java)

```java
createCapturedOutput(String key, Class<T> type)
createCapturedOutput(String key, Class<T> type, boolean highlighted)
```

### `CapturedOutputs` methods

| Method | Description |
|--------|-------------|
| `put(key: String, value: Any)` | Store by string key |
| `put(key: CapturedOutput<T>, value: T)` | Store by typed key |
| `outputs[key]` / `get(key)` | Retrieve by string key (throws if absent) |
| `outputs[key]` / `get(key: CapturedOutput<T>)` | Retrieve by typed key (throws if absent) |
| `getOrNull(key)` | Retrieve by string or typed key, returns `null` if absent |
| `contains(key)` | Check for presence by string or typed key |
| `values()` | All stored values as `Set<NamedValue>` |
| `highlightedValues()` | Only highlighted values |

---

## Highlighting

Highlighted outputs appear prominently in the report. Pass `highlighted = true` when creating the key:

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
val transactionId = capturedOutput<String>("transaction id", highlighted = true)

whenever { ctx ->
    val result = paymentGateway.submit(ctx.fixtures[paymentRequest])
    ctx.outputs.put(transactionId, result.transactionId)
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
static final CapturedOutput<String> TRANSACTION_ID =
    createCapturedOutput("transaction id", String.class, true);

// In whenever:
ctx.getOutputs().put(TRANSACTION_ID, result.getTransactionId());
```

</TabItem>
</Tabs>
