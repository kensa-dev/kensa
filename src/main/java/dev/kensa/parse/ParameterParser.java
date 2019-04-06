package dev.kensa.parse;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import dev.kensa.util.NameValuePair;

import java.util.Map;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class ParameterParser {

    private final MethodDeclaration methodDeclaration;

    public ParameterParser(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public Map<String, NameValuePair> parameters(Object[] parameterValues) {
        NodeList<Parameter> parameters = methodDeclaration.getParameters();

        return IntStream.range(0, parameters.size())
                 .mapToObj(index -> new NameValuePair(parameters.get(index).getNameAsString(), parameterValues[index]))
                 .collect(toMap(NameValuePair::name, identity()));
    }

}
