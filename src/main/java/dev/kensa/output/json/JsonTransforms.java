package dev.kensa.output.json;

import com.eclipsesource.json.*;
import dev.kensa.KensaException;
import dev.kensa.context.TestContainer;
import dev.kensa.render.Renderers;
import dev.kensa.sentence.Sentence;
import dev.kensa.state.TestInvocation;
import dev.kensa.util.DurationFormatter;
import dev.kensa.util.KensaMap;
import dev.kensa.util.NameValuePair;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public final class JsonTransforms {

    public static Function<TestContainer, JsonValue> toJsonWith(Renderers renderers) {
        return container ->
                Json.object()
                    .add("testClass", container.testClass().getName())
                    .add("displayName", container.displayName())
                    .add("state", container.state().description())
                    .add("notes", container.notes())
                    .add("issue", asJsonArray(container.issues()))
                    .add("tests", asJsonArray(
                            container.invocationData(),
                            invocationData -> Json.object()
                                                  .add("testMethod", invocationData.testMethod().getName())
                                                  .add("displayName", invocationData.displayName())
                                                  .add("notes", invocationData.notes())
                                                  .add("issue", asJsonArray(invocationData.issues()))
                                                  .add("state", invocationData.state().description())
                                                  .add("invocations", asJsonArray(
                                                          invocationData.invocations(),
                                                          invocation -> Json.object()
                                                                            .add("elapsedTime", DurationFormatter.format(invocation.elapsed()))
                                                                            .add("highlights", asJsonArray(invocation.highlights()))
                                                                            .add("acronyms", asJsonArray(invocation.acronyms()))
                                                                            .add("sentences", asJsonArray(invocation.sentences(), sentenceAsJson()))
                                                                            .add("parameters", asJsonArray(invocation.parameters().stream(), nvpAsJson()))
                                                                            .add("givens", asJsonArray(invocation.givens(), entryAsJson(renderers)))
                                                                            .add("capturedInteractions", asJsonArray(invocation.interactions(), entryAsJson(renderers)))
                                                                            .add("sequenceDiagram", invocation.sequenceDiagram().toString())
                                                                            .add("state", invocation.state().description())
                                                                            .add("executionException", executionExceptionFrom(invocation))
                                                  ))
                    ));
    }

    public static Function<TestContainer, JsonValue> toIndexJson(String id) {
        return container -> Json.object()
                                .add("id", id)
                                .add("testClass", container.testClass().getName())
                                .add("displayName", container.displayName())
                                .add("state", container.state().description())
                                .add("tests", asJsonArray(
                                        container.invocationData(),
                                        invocationData -> Json.object()
                                                              .add("testMethod", invocationData.testMethod().getName())
                                                              .add("displayName", invocationData.displayName())
                                                              .add("state", invocationData.state().description())
                                ));
    }

    public static Function<JsonValue, String> toJsonString() {
        return jv -> {
            try {
                StringWriter stringWriter = new StringWriter();
                jv.writeTo(stringWriter, WriterConfig.MINIMAL);
                return stringWriter.toString();
            } catch (IOException e) {
                throw new KensaException("Unable to write Json string", e);
            }
        };
    }

    private static Function<Sentence, JsonValue> sentenceAsJson() {
        return sentence -> asJsonArray(sentence.squashedTokens(), token -> Json.object().add("type", token.type().name()).add("value", token.asString()));
    }

    private static JsonArray asJsonArray(Stream<String> stream) {
        return asJsonArray(stream, Json::value);
    }

    private static <T> JsonArray asJsonArray(Stream<T> stream, Function<T, ? extends JsonValue> transformer) {
        return stream.map(transformer)
                     .collect(
                             Json::array,
                             JsonArray::add,
                             (first, second) -> second.forEach(first::add)
                     );
    }

    private static Function<KensaMap.Entry, JsonValue> entryAsJson(Renderers renderers) {
        return entry -> Json.object()
                            .add("name", entry.key())
                            .add("value", renderers.renderValueOnly(entry.value()))
                            .add("renderables", asJsonArray(renderers.renderAll(entry.value()), entriesAsJson()))
                            .add("attributes", asJsonArray(entry.attributes(), nvpAsJson()));
    }

    private static Function<Map.Entry<String, Object>, JsonValue> entriesAsJson() {
        return entry -> {
            JsonObject object = Json.object().add("name", entry.getKey());

            if(entry.getValue() instanceof Map) {
                Map map = (Map) entry.getValue();
                object.add("value", asJsonArray(map.entrySet().stream(), entriesAsJson()));
            } else {
                object.add("value", entry.getValue().toString());
            }

            return object;
        };
    }

    private static Function<NameValuePair, JsonObject> nvpAsJson() {
        return (nvp) -> Json.object()
                            .add("name", nvp.name())
                            .add("value", nvp.value().toString());
    }

    private static JsonObject executionExceptionFrom(TestInvocation invocation) {
        return invocation.executionException()
                         .map(throwable -> Json.object()
                                               .add("message", throwable.getMessage())
                                               .add("stackTrace", toString(throwable)))
                         .orElse(Json.object());
    }

    private static String toString(Throwable throwable) {
        StringWriter out = new StringWriter();
        throwable.printStackTrace(new PrintWriter(out));
        return out.toString();
    }

    private JsonTransforms() {
    }
}
