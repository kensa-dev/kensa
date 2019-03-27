package dev.kensa.parse;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

public class SimpleMethodMatchingVisitor extends GenericVisitorAdapter<MethodDeclaration, MethodCriteria> {
    @Override
    public MethodDeclaration visit(MethodDeclaration md, MethodCriteria criteria) {
        if (matches(md, criteria)) {
            return md;
        }

        return super.visit(md, criteria);
    }

    private boolean matches(MethodDeclaration md, MethodCriteria criteria) {
        if(md.getName().getIdentifier().equals(criteria.methodName())) {
            return md.hasParametersOfType(criteria.parameterTypes());
        }

        return false;
    }
}