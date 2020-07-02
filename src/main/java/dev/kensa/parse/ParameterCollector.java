package dev.kensa.parse;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import dev.kensa.util.DisplayableNamedValue;
import dev.kensa.util.Strings;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParameterCollector {

    private final MethodDeclaration methodDeclaration;

    ParameterCollector(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public List<DisplayableNamedValue> collect(Object[] parameterValues) {
        NodeList<Parameter> parameters = methodDeclaration.getParameters();

        return IntStream.range(0, parameters.size())
                 .mapToObj(index -> {
                     final String name = parameters.get(index).getNameAsString();
                     return new DisplayableNamedValue(
                             name,
                             Strings.unCamel(name),
                             parameterValues[index]);
                 })
                 .collect(Collectors.toList());
    }
}
