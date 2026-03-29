---
sidebar_position: 3
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Fixtures

Kensa Fixtures are collections of type-safe, lazily-created test data values. Each test invocation has its own discreet set of Fixtures. They are shared across `given`, `whenever`, and `then` steps via the context objects.

---

## Defining Fixtures

Fixtures must be defined inside a `FixtureContainer` object. This can be an instance of a Java class or a Kotlin object.
They carry a string key which must be unique — and a factory that creates the value.

Fixtures can depend on up to three other fixtures, the factory for dependent fixtures will receive the resolved parent values at creation time.

It is best to define fixtures statically by public properties, which you can then use when referencing them in tests.

### Primary Fixtures

A primary fixture has no dependencies — its factory takes no arguments.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
object MyFixtures : FixtureContainer {
    // Simple fixture
    val CustomerId = fixture("CustomerId") { "cust-${UUID.randomUUID()}" }
    
    // Highlighted in the report
    val OrderId = fixture("OrderId", highlighted = true) { someRandomOrderId() }
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
class MyFixtures implements FixtureContainer {
    private MyFixtures() {}

    public static final PrimaryFixture<String> CUSTOMER_ID = createFixture("CustomerId", () -> "cust-" + UUID.randomUUID());
    public static final PrimaryFixture<String> ORDER_ID = createFixture("CustomerId", true, () -> someRandomOrderId());
}
```

</TabItem>
</Tabs>

### Secondary Fixtures

A secondary fixture depends on one or more parent fixtures. Its factory receives the resolved parent values.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
// Depends on one parent
val customer = fixture("customer", CustomerId) { id ->
    Customer(id, "Jane Smith")
}

// Depends on two parents
val order = fixture("order", CustomerId, OrderId) { custId, ordId ->
    Order(ordId, custId, listOf(Product("widget")))
}

// Up to three parents are supported
val shipment = fixture("shipment", Customer, Order, WarehouseId) { cust, ord, whId ->
    Shipment(cust, ord, whId)
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
static final Fixture<Customer> CUSTOMER = createFixture("customer", CUSTOMER_ID,
    id -> new Customer(id, "Jane Smith"));

static final Fixture<Order> ORDER = createFixture("order", CUSTOMER_ID, ORDER_ID,
    (custId, ordId) -> new Order(ordId, custId, List.of(new Product("widget"))));
```

</TabItem>
</Tabs>

---

## Using Fixtures in Tests

Access fixtures through the context passed to each step. Fixture values are created lazily — the factory runs the first time `fixtures[key]` is called.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
class OrderTest : KensaTest, WithKotest {

    private val customerId = fixture("customer id") { "cust-123" }
    private val customer = fixture("customer", customerId) { id -> Customer(id, "Jane Smith") }

    @Test
    fun `places an order successfully`() {
        given {
            fixtures[customer]  // creates customer (and customerId) lazily
        }
        whenever { ctx ->
            val c = ctx.fixtures[customer]
            ctx.outputs.put("response", orderService.placeOrder(c))
        }
        then({ ctx -> ctx.outputs["response"] }) {
            it.statusCode shouldBe 201
        }
    }
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
class OrderTest implements KensaTest, WithAssertJ {

    static final Fixture<String> CUSTOMER_ID = createFixture("customer id", () -> "cust-123");
    static final Fixture<Customer> CUSTOMER = createFixture("customer", CUSTOMER_ID,
        id -> new Customer(id, "Jane Smith"));

    @Test
    void placesAnOrderSuccessfully() {
        given(ctx -> ctx.getFixtures().get(CUSTOMER));  // lazy creation
        whenever(ctx -> {
            Customer c = ctx.getFixtures().get(CUSTOMER);
            ctx.getOutputs().put("response", orderService.placeOrder(c));
        });
        then(ctx -> ctx.getOutputs().get("response"),
            response -> assertThat(response.statusCode()).isEqualTo(201));
    }
}
```

</TabItem>
</Tabs>

---

## Fixture API Reference

### `fixture()` factory (Kotlin)

```kotlin
// Primary — no dependencies
fun <T> fixture(key: String, highlighted: Boolean = false, factory: () -> T): PrimaryFixture<T>

// Secondary — 1 parent
fun <T, P1> fixture(key: String, parent: Fixture<P1>, highlighted: Boolean = false, factory: (P1) -> T): SecondaryFixture<T>

// Secondary — 2 parents
fun <T, P1, P2> fixture(key: String, parent1: Fixture<P1>, parent2: Fixture<P2>, highlighted: Boolean = false, factory: (P1, P2) -> T): SecondaryFixture<T>

// Secondary — 3 parents
fun <T, P1, P2, P3> fixture(key: String, parent1: Fixture<P1>, parent2: Fixture<P2>, parent3: Fixture<P3>, highlighted: Boolean = false, factory: (P1, P2, P3) -> T): SecondaryFixture<T>
```

### `createFixture()` factory (Java)

```java
createFixture(String key, Supplier<T> factory)
createFixture(String key, boolean highlighted, Supplier<T> factory)
createFixture(String key, Fixture<P1> parent, Function<P1, T> factory)
createFixture(String key, Fixture<P1> parent1, Fixture<P2> parent2, BiFunction<P1, P2, T> factory)
```

### `Fixtures` map

| Method / operator | Description |
|-------------------|-------------|
| `fixtures[fixture]` | Get (and lazily create) the fixture value |
| `fixtures.values()` | All fixture values as `Set<NamedValue>` |
| `fixtures.highlightedValues()` | Only highlighted fixture values |

---

## Highlighting

Set `highlighted = true` on any fixture to have its value appear prominently in the report.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
val transactionId = fixture("transaction id", highlighted = true) { "txn-${UUID.randomUUID()}" }
```

</TabItem>
<TabItem value="java" label="Java">

```java
static final Fixture<String> TRANSACTION_ID = createFixture("transaction id", true,
    () -> "txn-" + UUID.randomUUID());
```

</TabItem>
</Tabs>

Highlighted values are also accessible separately via `fixtures.highlightedValues()`, which Kensa uses to render them at the top of the report.
