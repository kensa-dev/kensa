package dev.kensa.parse;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import dev.kensa.util.NameValuePair;

import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

public class ParameterCollector {

    private final MethodDeclaration methodDeclaration;

    ParameterCollector(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public Set<NameValuePair> collect(Object[] parameterValues) {
        NodeList<Parameter> parameters = methodDeclaration.getParameters();

        return IntStream.range(0, parameters.size())
                 .mapToObj(index -> new NameValuePair(parameters.get(index).getNameAsString(), parameterValues[index]))
                 .collect(toSet());
    }
}
