package ru.dpohvar.varscript.trigger;

public interface TriggerHolder {

    public void stopTriggers();

    public int triggerCount();

    public Trigger[] getTriggers();

}
