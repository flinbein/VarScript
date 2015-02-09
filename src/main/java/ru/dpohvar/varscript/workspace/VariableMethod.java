package ru.dpohvar.varscript.workspace;

public interface VariableMethod {

    public static final Object SKIP_METHOD = new Object();

    public Object invoke(String name, Object[] args, VariableContainer current, VariableContainer requester) throws Exception;
}
