package dev.kensa.example;

public class JavaWithQualifiedConstants {

    enum JavaOrderStatus { PENDING, COMPLETE }
    enum JavaPaymentStatus { PENDING, SETTLED }

    void testWithQualifiedEnums() {
        theOrderIs(JavaOrderStatus.PENDING);
        andThePaymentIs(JavaPaymentStatus.PENDING);
    }

    void testWithEnumMethodCall() {
        theOrderIs(JavaOrderStatus.valueOf("PENDING"));
    }

    void testWithChainedAccess() {
        theNameIs(JavaOrderStatus.PENDING.name());
    }

    private void theOrderIs(JavaOrderStatus status) {}
    private void andThePaymentIs(JavaPaymentStatus status) {}
    private void theNameIs(String name) {}
}
