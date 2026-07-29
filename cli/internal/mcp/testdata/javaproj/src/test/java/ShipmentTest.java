class ShipmentFixtures implements FixtureContainer {
    public static final PrimaryFixture<String> CarrierFx = createFixture("Carrier", () -> "ACME");
    public static final SecondaryFixture<String> ConsignmentFx = createFixture("Consignment", CarrierFx, carrier -> carrier + "-1");
}

public class ShipmentTest implements KensaTest, WithHamcrest {

    private final JsonTextField aCarrier = new JsonTextField("/carrier");

    @Test
    void canDispatchAShipment() {
        given(aShipmentExists());
        whenever(theShipmentIsDispatched());
        then(theShipment(), hasStatus("DISPATCHED"));
    }
}
