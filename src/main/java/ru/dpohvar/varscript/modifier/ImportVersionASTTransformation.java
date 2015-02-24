package ru.dpohvar.varscript.modifier;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import ru.dpohvar.varscript.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.control.CompilePhase.CONVERSION;

@GroovyASTTransformation(phase=CONVERSION)
public class ImportVersionASTTransformation implements ASTTransformation {

    private final String cbVersionSuffix = ReflectionUtils.getCbVersionSuffix();
    private final String nmsVersionSuffix =  ReflectionUtils.getNmsVersionSuffix();

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        ModuleNode ast = source.getAST();
        List<ImportLater> importsLater = new ArrayList<ImportLater>();
        Iterator<ImportNode> iterator = ast.getImports().iterator();
        while (iterator.hasNext()) {
            ImportNode importNode = iterator.next();
            String className = importNode.getClassName();
            String modifiedClassName = getModifiedClassName(className);
            if (modifiedClassName == null) continue;
            ImportLater importLater = new ImportLater();
            importLater.annotations = importNode.getAnnotations();
            importLater.alias = importNode.getAlias();
            importLater.classNode = ClassHelper.make(modifiedClassName);
            importsLater.add(importLater);
            iterator.remove();
        }
        for (ImportLater importLater : importsLater) {
            ast.addImport(importLater.alias, importLater.classNode, importLater.annotations);
        }
    }

    protected String getModifiedClassName(String className){
        boolean useCb = className.startsWith( "org.bukkit.craftbukkit");
        boolean useNms = className.startsWith("net.minecraft.server");
        if (!useCb && !useNms) return null;
        String[] sp = className.split("\\.");
        if (sp.length < 3) return null;
        String prefix = sp[0]+"."+sp[1]+"."+sp[2];
        String versionTag = null;
        if (sp.length > 3) versionTag = sp[3];
        if (versionTag != null && !versionTag.matches("v.+_.+")) versionTag = null;
        if (versionTag == null) {
            if (useCb && cbVersionSuffix == null) return null;
            if (useNms && nmsVersionSuffix == null) return null;
        } else {
            if (useCb && versionTag.equals(cbVersionSuffix)) return null;
            if (useNms && versionTag.equals(nmsVersionSuffix)) return null;
        }

        int endIndex = 4;
        if (versionTag == null) endIndex = 3;
        String postfix = "";
        for (int i= endIndex; i<sp.length; i++) postfix += "."+sp[i];

        if (useCb) {
            if (cbVersionSuffix == null) return prefix + postfix;
            else return prefix + "." + cbVersionSuffix + postfix;
        } else {
            if (nmsVersionSuffix == null) return prefix + postfix;
            else return prefix + "." + nmsVersionSuffix + postfix;
        }
    }

    static class ImportLater{
        public List<AnnotationNode> annotations;
        public String alias;
        public String packageName;
        public ClassNode classNode;
    }
}
