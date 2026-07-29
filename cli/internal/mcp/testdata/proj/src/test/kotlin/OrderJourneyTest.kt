class OrderJourneyTest : KensaTest, WithKotest {

    @Test
    fun canPlaceAnOrder() {
        given(aCustomerExists())
        and(theCatalogueContains(fixtures(OrderFixtures.ItemFx)))

        whenever(theCustomerPlacesAnOrder())

        then(theOrder()) { shouldBeConfirmed() }
        and(theOrderReference()) { shouldStartWith("OR-") }
    }
}
