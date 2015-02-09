package ru.dpohvar.varscript.workspace.decorate;

import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.VariableMethod;
import ru.dpohvar.varscript.workspace.Workspace;

import java.io.File;
import java.util.Arrays;

public class GroovyScriptMethod implements VariableMethod {

    private final File scriptFolder;

    public GroovyScriptMethod(File scriptFolder){
        this.scriptFolder = scriptFolder;
    }

    @Override
    public Object invoke(String name, Object[] args, VariableContainer current, VariableContainer requester) throws Exception {
        File scriptFile = new File(scriptFolder, name+".groovy");
        if (!scriptFile.isFile()) return SKIP_METHOD;
        Caller caller = (Caller) requester.getHardVariables().get("caller");
        Workspace workspace = (Workspace) requester.getHardVariables().get("workspace");
        VariableContainer variables = new VariableContainer(workspace.getWorkspaceVariables());
        variables.getSoftVariables().put("args", Arrays.asList(args));
        return workspace.executeScript(caller, scriptFile, variables);
    }
}
