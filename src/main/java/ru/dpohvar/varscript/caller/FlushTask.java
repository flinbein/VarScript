package ru.dpohvar.varscript.caller;

import ru.dpohvar.varscript.workspace.CallerScript;

public class FlushTask implements Runnable{

    private final CallerScript caller;

    public FlushTask(CallerScript script) {
        this.caller = script;
    }

    @Override
    public void run() {
        caller.flush();
    }
}
