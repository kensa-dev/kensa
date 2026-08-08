package dev.kensa.example;

import dev.kensa.RenderedValue;
import dev.kensa.RenderedValueContainer;

public class JavaWithContainerChains {

    public static class Stub {
        public Action sends(Request r) { return new Action(); }
    }

    public static class UseCase {
        @RenderedValue
        public final Stub stub = new Stub();
        public final String plain = "p";
    }

    public static class Request {}
    public static class Action {}

    @RenderedValueContainer
    private final UseCase heldCase = new UseCase();

    private void whenever(Action a) {}
    private void sendIt(Object a) {}
    private Request aRequest() { return new Request(); }

    public void chainWithTrailingCall(@RenderedValueContainer UseCase useCase) {
        whenever(useCase.stub.sends(aRequest()));
    }

    public void bareChain(@RenderedValueContainer UseCase useCase) {
        sendIt(useCase.stub);
    }

    public void unannotatedMember(@RenderedValueContainer UseCase useCase) {
        sendIt(useCase.plain);
    }

    public void fieldContainerChain() {
        whenever(heldCase.stub.sends(aRequest()));
    }
}
