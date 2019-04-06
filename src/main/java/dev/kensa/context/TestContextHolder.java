package dev.kensa.context;

public final class TestContextHolder {

    private static final ThreadLocal<TestContext> holder = new ThreadLocal<>();

    public static void bindToThread(TestContext testContext) {
        holder.set(testContext);
    }

    public static TestContext testContext() {
        return holder.get();
    }

    public static void clearFromThread() {
        holder.remove();
    }
}
