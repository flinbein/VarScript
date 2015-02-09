package ru.dpohvar.varscript.workspace;

import groovy.lang.Binding;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VariableContainer extends Binding {

    Map<String,Object> hardVariables = new HashMap<String,Object>();
    Map<String,Object> softVariables = new HashMap<String,Object>();
    Map<String,VariableGetter> hardGetters = new HashMap<String,VariableGetter>();
    Map<String,VariableGetter> softGetters = new HashMap<String,VariableGetter>();
    Map<String,VariableSetter> hardSetters = new HashMap<String,VariableSetter>();
    Map<String,VariableSetter> softSetters = new HashMap<String,VariableSetter>();
    List<VariableGetter> hardDynamicGetters = new LinkedList<VariableGetter>();
    List<VariableGetter> softDynamicGetters = new LinkedList<VariableGetter>();
    List<VariableSetter> hardDynamicSetters = new LinkedList<VariableSetter>();
    List<VariableSetter> softDynamicSetters = new LinkedList<VariableSetter>();
    List<VariableMethod> hardDynamicMethods = new LinkedList<VariableMethod>();

    // Get variable:
    // -> hardGetters, hardDynamicGetters, hardVariables (parent, then self)
    // -> softGetters, softDynamicGetters, softVariables (self, then parent)
    // -> throw MissingPropertyException

    // Set variable:
    // -> hardSetters, hardDynamicSetters (parent, then self)
    // -> softSetters, softDynamicSetters (self, then parent)
    // -> put to softVariables

    private final VariableContainer parent;

    public VariableContainer(){
        this.parent = null;
    }

    public VariableContainer(VariableContainer parent){
        this.parent = parent;
    }

    public Map<String, Object> getHardVariables() {
        return hardVariables;
    }

    public Map<String, Object> getSoftVariables() {
        return softVariables;
    }

    public VariableContainer getParent() {
        return parent;
    }

    public List<VariableGetter> getHardDynamicGetters() {
        return hardDynamicGetters;
    }

    public List<VariableGetter> getSoftDynamicGetters() {
        return softDynamicGetters;
    }

    public List<VariableSetter> getHardDynamicSetters() {
        return hardDynamicSetters;
    }

    public List<VariableSetter> getSoftDynamicSetters() {
        return softDynamicSetters;
    }

    public Map<String, VariableSetter> getHardSetters() {
        return hardSetters;
    }

    public Map<String, VariableSetter> getSoftSetters() {
        return softSetters;
    }

    public Map<String, VariableGetter> getSoftGetters() {
        return softGetters;
    }

    public Map<String, VariableGetter> getHardGetters() {
        return hardGetters;
    }

    public List<VariableMethod> getHardDynamicMethods() {
        return hardDynamicMethods;
    }

    Object getHardVariable(String name, VariableContainer requester){
        if (parent != null) try {
            return parent.getHardVariable(name, requester);
        } catch (MissingPropertyException ignored){}
        Object result;
        for (VariableGetter hardVariableGetter : hardDynamicGetters) {
            result = hardVariableGetter.getValue(name, this, requester);
            if (result != VariableGetter.SKIP_GETTER) return result;
        }
        VariableGetter variableGetter = hardGetters.get(name);
        if (variableGetter != null) {
            result = variableGetter.getValue(name, this, requester);
            if (result != VariableGetter.SKIP_GETTER) return result;
        }
        result = hardVariables.get(name);
        if (result == null && !hardVariables.containsKey(name)) {
            throw new MissingPropertyException(name, this.getClass());
        }
        return result;
    }


    Object invokeHardMethod(String name, Object[] args, VariableContainer requester) throws Exception{
        if (parent != null) try {
            return parent.invokeHardMethod(name, args, requester);
        } catch (MissingMethodException ignored){}
        Object result;
        for (VariableMethod method : hardDynamicMethods) {
            result = method.invoke(name, args, this, requester);
            if (result != VariableMethod.SKIP_METHOD) return result;
        }
        throw new MissingMethodException(name, this.getClass(), args);
    }

    Object getSoftVariable(String name, VariableContainer requester) throws MissingPropertyException{
        Object result;
        for (VariableGetter softVariableGetter : softDynamicGetters) {
            result = softVariableGetter.getValue(name, this, requester);
            if (result != VariableGetter.SKIP_GETTER) return result;
        }
        VariableGetter variableGetter = softGetters.get(name);
        if (variableGetter != null) {
            result = variableGetter.getValue(name, this, requester);
            if (result != VariableGetter.SKIP_GETTER) return result;
        }
        result = softVariables.get(name);
        if (result == null && !softVariables.containsKey(name)) {
            if (parent != null) return parent.getSoftVariable(name, requester);
            throw new MissingPropertyException(name, this.getClass());
        }
        return result;
    }

    public boolean setHardVariable(String name, Object value, VariableContainer requester){
        if (parent != null) {
            boolean success = parent.setHardVariable(name, value, requester);
            if (success) return true;
        }
        VariableSetter variableSetter = hardSetters.get(name);
        if (variableSetter != null && variableSetter.setValue(name, value, this, requester)) return true;
        for (VariableSetter setter : hardDynamicSetters) {
            boolean success = setter.setValue(name, value, this, requester);
            if (success) return true;
        }
        return false;
    }

    public boolean setSoftVariable(String name, Object value, VariableContainer requester){
        VariableSetter variableSetter = softSetters.get(name);
        if (variableSetter != null && variableSetter.setValue(name, value, this, requester)) return true;
        for (VariableSetter setter : softDynamicSetters) {
            boolean success = setter.setValue(name, value, this, requester);
            if (success) return true;
        }
        if (parent != null) {
            boolean success = parent.setSoftVariable(name, value, requester);
            if (success) return true;
        }
        return false;
    }

    @Override
    public Object getVariable(String name) throws MissingPropertyException{
        try {
            return getHardVariable(name, this);
        } catch (MissingPropertyException ignored){
            return getSoftVariable(name, this);
        }
    }

    @Override
    public boolean hasVariable(String name) {
        if (hardVariables.containsKey(name)) return true;
        if (softVariables.containsKey(name)) return true;
        return parent != null && parent.hasVariable(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        if (setHardVariable(name, value, this)) return;
        if (setSoftVariable(name, value, this)) return;
        softVariables.put(name, value);
    }
}
















