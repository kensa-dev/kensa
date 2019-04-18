package dev.kensa.parse;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
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

    MethodParser(MethodDeclaration methodDeclaration, Function<String, Optional<NameValuePair>> identifierProvider) {
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
        SentenceBuilder builder = new SentenceBuilder();

        return statement.getChildNodes()
                        .stream()
                        .map(node -> toSentence(builder, node))
                        .map(SentenceBuilder::build)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(String.format("Unable to construct sentence from statement [%s]", statement.toString())));
    }

    private SentenceBuilder toSentence(SentenceBuilder builder, Node node) {
        int startLine = startLineOf(node);

        append(node, startLine, builder);

        return builder;
    }

    private int append(Node node, int lastLineNumber, SentenceBuilder builder) {
        int startLine = startLineOf(node);

        if (startLine > lastLineNumber) {
            lastLineNumber = startLine;
            builder.appendNewLine();
        }

        if (node instanceof NameExpr) {
            String identifier = ((NameExpr) node).getName().getIdentifier();
            builder.appendIdentifier(replaceWithRealValueOf(identifier));
            return lastLineNumber;
        }

        if (node instanceof SimpleName) {
            String identifier = ((SimpleName) node).getIdentifier();
            builder.append(identifier);
            return lastLineNumber;
        }

        if (node instanceof LiteralExpr) {
            LiteralExpr le = (LiteralExpr) node;

            le.ifLongLiteralExpr(e -> builder.appendLiteral(e.getValue()));
            le.ifDoubleLiteralExpr(e -> builder.appendLiteral(e.getValue()));
            le.ifIntegerLiteralExpr(e -> builder.appendLiteral(e.getValue()));
            le.ifBooleanLiteralExpr(e -> builder.appendLiteral(String.valueOf(e.getValue())));
            le.ifNullLiteralExpr(e -> builder.appendLiteral("null"));
            le.ifStringLiteralExpr(e -> builder.appendStringLiteral(e.getValue()));

            return lastLineNumber;
        }

        for (Node n : node.getChildNodes()) {
            lastLineNumber = append(n, lastLineNumber, builder);
        }

        return lastLineNumber;
    }

    private int startLineOf(Node node) {
        return node.getRange()
                   .map(range -> range.begin.line)
                   .orElse(0);
    }

    private String replaceWithRealValueOf(String identifier) {
        return identifierProvider.apply(identifier)
                                 .map(NameValuePair::value)
                                 .map(Object::toString)
                                 .orElse(identifier);
    }
}
