package dev.kensa.parse;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import dev.kensa.sentence.Sentence;
import dev.kensa.sentence.SentenceBuilder;
import dev.kensa.sentence.Sentences;
import dev.kensa.util.NameValuePair;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.kensa.parse.SentenceCollector.asSentences;
import static java.util.stream.Collectors.toList;

public class MethodParser {

    private final List<NameValuePair> parameters;
    private final MethodDeclaration methodDeclaration;

    MethodParser(MethodDeclaration methodDeclaration, Object[] parameterValues) {
        this.methodDeclaration = methodDeclaration;
        this.parameters = parameterDescriptorsFrom(methodDeclaration.getParameters(), parameterValues);
    }

    public Sentences sentences() {
        return methodDeclaration.getBody()
                                .map(BlockStmt::getStatements).stream().flatMap(Collection::stream)
                                .map(this::toSentence)
                                .collect(asSentences());
    }

    public List<NameValuePair> parameters() {
        return parameters;
    }

    private List<NameValuePair> parameterDescriptorsFrom(NodeList<Parameter> parameters, Object[] parameterValues) {
        var index = new AtomicInteger();
        return parameters.stream()
                         .map(parameter -> {
                             String name = parameter.getNameAsString();
                             Object value = parameterValues[index.getAndIncrement()];
                             return new NameValuePair(name, value);
                         })
                         .collect(toList());
    }

    private Sentence toSentence(Statement statement) {
        return statement.getChildNodes()
                        .stream()
                        .map(node -> buildSentenceFrom(node, new SentenceBuilder()))
                        .map(SentenceBuilder::build)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(String.format("Unable to construct sentence from statement [%s]", statement.toString())));
    }

    private SentenceBuilder buildSentenceFrom(Node node, SentenceBuilder builder) {
        if (node instanceof NameExpr) {
            String identifier = ((NameExpr) node).getName().getIdentifier();
            return builder.appendParameter(replaceWithParameter(identifier));
        }

        if (node instanceof SimpleName) {
            return builder.append(((SimpleName) node).getIdentifier());
        }

        if (node instanceof StringLiteralExpr) {
            return builder.append(((StringLiteralExpr) node).asString());
        }

        node.getChildNodes()
            .forEach(n -> buildSentenceFrom(n, builder));

        return builder;
    }

    private String replaceWithParameter(String identifier) {
        return parameters.stream()
                         .filter(pd -> pd.name().equals(identifier))
                         .map(pd -> pd.value().toString())
                         .findFirst()
                         .orElse(identifier);
    }
}
