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
import dev.kensa.sentence.SentenceToken;
import dev.kensa.sentence.Sentences;
import dev.kensa.util.NamedValue;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.kensa.parse.SentenceCollector.asSentences;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class MethodParser {

    private final ValueAccessors valueAccessors;
    private final Set<String> highlightedValues;
    private final Set<String> keywords;
    private final Set<String> acronyms;
    private final Set<NamedValue> expandableMethods;


    MethodParser(ValueAccessors valueAccessors,
                 Set<String> highlightedValues,
                 Set<String> keywords,
                 Set<String> acronyms,
                 Set<NamedValue> expandableMethods
    ) {
        this.valueAccessors = valueAccessors;
        this.highlightedValues = highlightedValues;
        this.keywords = keywords;
        this.acronyms = acronyms;
        this.expandableMethods = expandableMethods;
    }

    Sentences parse(MethodDeclaration methodDeclaration) {
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
        int thisLineNumber = startLineOf(node);
        builder.markLineNumber(thisLineNumber);

        if (isNotAScenarioCall(node, builder)) {
            if (node instanceof MethodCallExpr) {
                String methodName = ((MethodCallExpr) node).getNameAsString();


                Optional<MethodDeclaration> methodDeclaration = expandableMethod(methodName);
                if (methodDeclaration.isPresent()) {
                    Statement statement = methodDeclaration.get().getBody().get().getStatement(0);
                    Sentence nested = toSentence(statement);
                    SentenceBuilder placeholderBuilder = new SentenceBuilder(thisLineNumber, highlightedValues, keywords, acronyms);
                    parseChildrenOf(node, placeholderBuilder);
                    Sentence placeholderSentence = placeholderBuilder.build();

                    builder.appendExpandable(placeholderSentence.squashedTokens().map(SentenceToken::value).collect(joining(" ")), nested.squashedTokens().collect(toList()));
                } else {
                    parseChildrenOf(node, builder);
                }
            } else if (node instanceof NameExpr) {
                String identifier = ((NameExpr) node).getName().getIdentifier();
                builder.appendIdentifier(valueAccessors.realValueOf(identifier)
                                                       .orElse(identifier)
                );
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
                parseChildrenOf(node, builder);
            }
        }
    }

    private void parseChildrenOf(Node parent, SentenceBuilder builder) {
        for (Node n : parent.getChildNodes()) {
            append(n, builder);
        }
    }

    private Optional<MethodDeclaration> expandableMethod(String methodName) {
        return expandableMethods.stream()
                                .filter(nv -> nv.name().equals(methodName))
                                .map(nv -> (MethodDeclaration) nv.value())
                                .findAny();
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