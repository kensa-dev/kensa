// Snippet source for kensa.dev/docs/api/configuration.md — Java sequence-diagram configuration
package apidocs;

import dev.kensa.Configuration;
import dev.kensa.KensaConfigurationProvider;
import dev.kensa.SequenceDiagramConfiguration;

public class MyKensaConfiguration implements KensaConfigurationProvider {

    private final Configuration configuration = new Configuration();

    public MyKensaConfiguration() {
        SequenceDiagramConfiguration diagram = configuration.getSequenceDiagram();
        diagram.title("Order placement");
        diagram.actor("User");
        diagram.participant("Frontend");
        diagram.participant("Orchestration");
        diagram.database("OrderStore");
        diagram.queue("Events");
        diagram.hideUnlinked();
    }

    @Override
    public Configuration invoke() {
        return configuration;
    }
}
