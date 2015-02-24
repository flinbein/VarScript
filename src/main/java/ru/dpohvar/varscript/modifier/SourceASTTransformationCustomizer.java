package ru.dpohvar.varscript.modifier;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MetaClass;
import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.ASTTransformation;

public class SourceASTTransformationCustomizer extends ASTTransformationCustomizer{

    private transient MetaClass metaClass;
    protected ASTTransformation transformation;
    private SourceUnit visitedSource;

    public SourceASTTransformationCustomizer(ASTTransformation transformation) {
        super(transformation);
        this.transformation = transformation;
    }

    @Override
    public Object getProperty(String property) {
        return getMetaClass().getProperty(this, property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        getMetaClass().setProperty(this, property, newValue);
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        return getMetaClass().invokeMethod(this, name, args);
    }

    @Override
    public MetaClass getMetaClass() {
        if (metaClass == null) {
            metaClass = InvokerHelper.getMetaClass(getClass());
        }
        return metaClass;
    }

    @Override
    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        if (visitedSource == source) return;
        visitedSource = source;
        transformation.visit(new ASTNode[]{classNode}, source);
    }
}
