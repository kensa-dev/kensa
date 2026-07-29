object OrderFixtures : FixtureContainer {
    val ItemFx = fixture("Item") { "WIDGET-1" }
    val QuantityFx = fixture("Quantity") { 5 }
}

object ShippingFixtures : FixtureContainer {
    val CarrierFx = fixture("Carrier") { "ACME" }

    @Fixture("Consignment")
    fun consignmentFor(weight: Int) = fixture { Consignment(weight) }
}

class InProcessOrderTest : KensaTest, WithKotest {
    private val aQuantityField: JsonIntField get() = JsonIntField("/quantity")
    private val aCarrierName = stringField("aCarrierName") { it.carrier }
}

fun primeSupplier(reference: String): Action<GivensContext> = Action { post("/http-stub/prime/$reference") }

@ExpandableSentence
private fun theResponseShows(@RenderedValue id: String) { id shouldBe "X" }

fun theStatusSettles() = thenEventually(scenario.theStatus()) { shouldBeConfirmed() }
fun theStatusHolds() = thenContinually(scenario.theStatus()) { shouldBeConfirmed() }
