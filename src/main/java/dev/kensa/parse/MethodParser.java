package dev.kensa.parse;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import dev.kensa.sentence.Sentence;
import dev.kensa.sentence.SentenceBuilder;
import dev.kensa.sentence.Sentences;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static dev.kensa.parse.SentenceCollector.asSentences;

class MethodParser {

    private final MethodDeclaration methodDeclaration;
    private final ValueAccessors valueAccessors;
    private final Set<String> highlightedValues;
    private final Set<String> keywords;
    private final Set<String> acronyms;

    MethodParser(MethodDeclaration methodDeclaration, ValueAccessors valueAccessors, Set<String> highlightedValues, Set<String> keywords, Set<String> acronyms) {
        this.methodDeclaration = methodDeclaration;
        this.valueAccessors = valueAccessors;
        this.highlightedValues = highlightedValues;
        this.keywords = keywords;
        this.acronyms = acronyms;
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
        SentenceBuilder builder = new SentenceBuilder(startLineOf(node), highlightedValues, keywords, acronyms);

        append(node, builder);

        return builder;
    }

    private void append(Node node, SentenceBuilder builder) {
        builder.markLineNumber(startLineOf(node));

        if (isNotAScenarioCall(node, builder)) {
            if (node instanceof NameExpr) {
                String identifier = ((NameExpr) node).getName().getIdentifier();
                valueAccessors.realValueOf(identifier)
                        .<Runnable>map(v -> () -> builder.appendIdentifier(v))
                        .orElse(() -> builder.append(identifier)).run();
            } else if (node instanceof SimpleName) {
                String identifier = ((SimpleName) node).getIdentifier();
                builder.append(identifier);
            } else if (node instanceof LiteralExpr) {
                LiteralExpr le = (LiteralExpr) node;

                le.ifLongLiteralExpr(e -> builder.appendLiteral(e.getValue()));
                le.ifDoubleLiteralExpr(e -> builder.appendLiteral(e.getValue()));
                le.ifIntegerLiteralExpr(e -> builder.appendLiteral(e.getValue()));
                le.ifBooleanLiteralExpr(e -> builder.appendLiteral(String.valueOf(e.getValue())));
                le.ifNullLiteralExpr(e -> builder.appendLiteral("null"));
                le.ifStringLiteralExpr(e -> builder.appendStringLiteral(e.getValue()));
            } else {
                for (Node n : node.getChildNodes()) {
                    append(n, builder);
                }
            }
        }
    }

    private boolean isNotAScenarioCall(Node node, SentenceBuilder builder) {
        if (node instanceof MethodCallExpr) {
            MethodCallExpr mce = (MethodCallExpr) node;
            return !mce.getScope()
                       .map(e -> e.isNameExpr() ? ((NameExpr) e).getNameAsString() : null)
                       .flatMap(n -> valueAccessors.realValueOf(n, mce.getNameAsString()))
                       .map(builder::appendIdentifier)
                       .isPresent();
        }

        return true;
    }

    private int startLineOf(Node node) {
        return node.getRange()
                   .map(range -> range.begin.line)
                   .orElse(0);
    }
}