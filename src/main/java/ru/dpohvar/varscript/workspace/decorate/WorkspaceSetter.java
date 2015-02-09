package ru.dpohvar.varscript.workspace.decorate;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.VariableSetter;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

/**
 * Setter for variable "workspace".
 * Takes string value.
 * Allows you to change workspace: {@code
 *   /> workspace = "myNewWorkspace"
 * }
 */
public class WorkspaceSetter implements VariableSetter{

    private final WorkspaceService service;

    public WorkspaceSetter(WorkspaceService service){
        this.service = service;
    }

    @Override
    public boolean setValue(String name, Object value, VariableContainer current, VariableContainer requester) {
        if (!name.equals("workspace")) return false;
        String workspaceName = DefaultGroovyMethods.toString(value);
        Caller caller = (Caller) requester.getHardVariables().get("caller");
        service.setWorkspaceName(caller.getSender(), workspaceName);
        return true;
    }
}
