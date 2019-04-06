package dev.kensa.parse;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.kensa.parse.SentenceCollector.asSentences;

class MethodParser {

    private final MethodDeclaration methodDeclaration;
    private final Function<String, Optional<NameValuePair>> identifierProvider;

    MethodParser(
            MethodDeclaration methodDeclaration,
            Function<String, Optional<NameValuePair>> identifierProvider
    ) {
        this.methodDeclaration = methodDeclaration;
        this.identifierProvider = identifierProvider;
    }

    Sentences sentences() {
        return methodDeclaration.getBody()
                                .map(BlockStmt::getStatements)
                                .map(Collection::stream)
                                .orElse(Stream.empty())
                                .map(this::toSentence)
                                .collect(asSentences());
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
            return builder.appendParameter(replaceWithRealValue(identifier));
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

    private String replaceWithRealValue(String identifier) {
        return identifierProvider.apply(identifier)
                                 .map(NameValuePair::value)
                                 .map(Object::toString)
                                 .orElse(identifier);
    }
}
