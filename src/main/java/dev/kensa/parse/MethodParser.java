package dev.kensa.parse;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import dev.kensa.render.Renderers;
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
    private final Renderers renderers;
    private final Function<String, Optional<NameValuePair>> identifierProvider;

    MethodParser(MethodDeclaration methodDeclaration, Renderers renderers, Function<String, Optional<NameValuePair>> identifierProvider) {
        this.methodDeclaration = methodDeclaration;
        this.renderers = renderers;
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
                        .map(this::toSentence)
                        .map(SentenceBuilder::build)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(String.format("Unable to construct sentence from statement [%s]", statement.toString())));
    }

    private SentenceBuilder toSentence(Node node) {
        SentenceBuilder builder = new SentenceBuilder(startLineOf(node));

        append(node, builder);

        return builder;
    }

    private void append(Node node, SentenceBuilder builder) {
        builder.markLineNumber(startLineOf(node));

        if (node instanceof NameExpr) {
            String identifier = ((NameExpr) node).getName().getIdentifier();
            builder.appendIdentifier(replaceWithRealValueOf(identifier));
        }

        if (node instanceof SimpleName) {
            String identifier = ((SimpleName) node).getIdentifier();
            builder.append(identifier);
        }

        if (node instanceof LiteralExpr) {
            LiteralExpr le = (LiteralExpr) node;

            le.ifLongLiteralExpr(e -> builder.appendLiteral(e.getValue()));
            le.ifDoubleLiteralExpr(e -> builder.appendLiteral(e.getValue()));
            le.ifIntegerLiteralExpr(e -> builder.appendLiteral(e.getValue()));
            le.ifBooleanLiteralExpr(e -> builder.appendLiteral(String.valueOf(e.getValue())));
            le.ifNullLiteralExpr(e -> builder.appendLiteral("null"));
            le.ifStringLiteralExpr(e -> builder.appendStringLiteral(e.getValue()));
        }

        for (Node n : node.getChildNodes()) {
            append(n, builder);
        }
    }

    private int startLineOf(Node node) {
        return node.getRange()
                   .map(range -> range.begin.line)
                   .orElse(0);
    }

    private String replaceWithRealValueOf(String identifier) {
        return identifierProvider.apply(identifier)
                                 .map(NameValuePair::value)
                                 .map(renderers::render)
                                 .orElse(identifier);
    }
}
