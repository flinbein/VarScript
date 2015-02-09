package ru.dpohvar.varscript.workspace.decorate;

import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.VariableSetter;

public class AbsoluteSetter implements VariableSetter {

    @Override
    public boolean setValue(String name, Object value, VariableContainer current, VariableContainer requester) {
        current.getSoftVariables().put(name, value);
        return true;
    }
}
