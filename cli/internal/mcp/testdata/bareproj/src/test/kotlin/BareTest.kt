class BareTest : KensaTest {
    @Test
    fun canDoAThing() {
        given(aThing())
        whenever(itHappens())
        then(theOutcome())
    }
}
