package ru.dpohvar.varscript.workspace;

public interface VariableSetter {

    public boolean setValue(String name, Object value, VariableContainer current, VariableContainer requester);
}
