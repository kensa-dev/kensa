// Snippet source for kensa.dev/docs/quickstart/testng-quickstart.md §5 Java tab
package quickstart;

import dev.kensa.testng.KensaTest;
import dev.kensa.assertj.WithAssertJ;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.annotations.Listeners;

public class TestNgJavaListenerSnippet implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
        // start stubs, register fixtures, call Kensa.configure()...
    }

    @Override
    public void onFinish(ISuite suite) {
        // close stubs and other resources
    }
}

@Listeners(TestNgJavaListenerSnippet.class)
abstract class MyAppJavaTest implements KensaTest, WithAssertJ {}
