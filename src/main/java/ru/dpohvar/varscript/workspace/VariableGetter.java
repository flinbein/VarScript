package ru.dpohvar.varscript.workspace;

public interface VariableGetter {

    public static final Object SKIP_GETTER = new Object();

    public Object getValue(String name, VariableContainer current, VariableContainer requester);
}
