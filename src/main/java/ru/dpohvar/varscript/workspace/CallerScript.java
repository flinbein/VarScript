package ru.dpohvar.varscript.workspace;

import groovy.lang.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.caller.FlushTask;

public abstract class CallerScript extends Script {

    private Caller caller;
    private BukkitScheduler scheduler;
    private Plugin plugin;
    private StringBuilder printCache = new StringBuilder();
    private BukkitTask flushBukkitTask = null;

    public Caller getCaller() {
        return caller;

    }

    public void setCaller(Caller caller) {
        this.caller = caller;
        this.plugin = caller.getService().getPlugin();
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void println() {
        println("");
    }

    @Override
    public void print(Object value) {
        String chars = DefaultGroovyMethods.toString(value);
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            if (c == '\n') {
                caller.sendPrintMessage(printCache.toString());
                printCache = new StringBuilder();
            } else {
                printCache.append(c);
            }
        }
        if (flushBukkitTask == null) flushBukkitTask = scheduler.runTask(plugin, new FlushTask(this));
    }

    @Override
    public void println(Object value) {
        String val = DefaultGroovyMethods.toString(value);
        if (printCache.length() > 0) val = printCache.toString() + val;
        caller.sendPrintMessage(val);
    }

    @Override
    public void printf(String format, Object value) {
        String result = String.format(format, value);
        print(result);
    }

    @Override
    public void printf(String format, Object[] values) {
        String result = String.format(format, values);
        print(result);
    }

    public synchronized void flush(){
        if (printCache.length() > 0) caller.sendPrintMessage(printCache.toString());
        flushBukkitTask = null;
        printCache = new StringBuilder();
    }

    @Override
    public Object invokeMethod(String name, Object args){
        Object[] arguments;
        if (args instanceof Object[]) arguments = (Object[]) args;
        else arguments = new Object[]{args};
        try {
            return super.invokeMethod(name, args);
        } catch (MissingMethodException mme) {
            VariableContainer variables = (VariableContainer) getBinding();
            try {
                return variables.invokeHardMethod(name, arguments, variables);
            } catch (MissingMethodException ignored){
            } catch (Exception e){
                throw new RuntimeException(e.getMessage(), e);
            }
            try {
                Object boundClosure = getProperty(name);
                if (boundClosure != null && boundClosure instanceof Closure) {
                    return ((Closure) boundClosure).call(arguments);
                }
                throw mme;
            } catch (MissingPropertyException mpe) {
                throw mme;
            }
        }
    }
}
