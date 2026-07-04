// Snippet source for kensa.dev/docs/api/log-tabs.md — Java registration
package apidocs;

import dev.kensa.Configuration;
import dev.kensa.KensaConfigurationProvider;
import dev.kensa.service.logs.LogPatterns;
import dev.kensa.service.logs.LogQueryService;
import dev.kensa.service.logs.LogQueryServiceRegistry;
import dev.kensa.service.logs.docker.DockerCliLogQueryService;
import dev.kensa.service.logs.docker.ProcessDockerLogsRunner;
import dev.kensa.tabs.InvocationIdentifierProvider;
import dev.kensa.tabs.KensaTabContext;
import kotlin.jvm.JvmClassMappingKt;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LogTabsJavaSnippets implements KensaConfigurationProvider {

    public static class TrackingIdProvider implements InvocationIdentifierProvider {
        @Override
        public @Nullable String identifier(KensaTabContext ctx) {
            return "TRK-1";
        }
    }

    private final Configuration configuration = new Configuration();

    public LogTabsJavaSnippets() {
        configuration.registerTabService(JvmClassMappingKt.getKotlinClass(LogQueryService.class), () -> {
            LogQueryServiceRegistry registry = new LogQueryServiceRegistry();
            registry.register("appLog", sourceId -> new DockerCliLogQueryService(
                    List.of(new DockerCliLogQueryService.DockerSource(sourceId, "my-app")),
                    LogPatterns.INSTANCE.idField("TrackingId", List.of(":")),
                    "***********",
                    new ProcessDockerLogsRunner()
            ));
            return registry.build();
        });
    }

    @Override
    public Configuration invoke() {
        return configuration;
    }
}
