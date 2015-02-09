package ru.dpohvar.varscript.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.scheduler.BukkitScheduler;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.List;

public class GroovyBufferRunner {

    private final Caller caller;

    public GroovyBufferRunner(Caller caller){
        this.caller = caller;
    }

    public void run(List<String> buffer){
        String script = StringUtils.join(buffer, '\n');
        Workspace currentWorkspace = caller.getCurrentWorkspace();
        try {
            Object result = currentWorkspace.executeScript(caller, script, null);
            if (result != null) caller.sendMessage(DefaultGroovyMethods.toString(result));
        } catch (Exception e) {
            caller.sendThrowable(e);
        }
    }

    public void runNextTick(List<String> buffer){
        VarScript plugin = caller.getService().getPlugin();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTask(plugin, new RunTask(this,buffer));
    }

    static class RunTask implements Runnable{

        private final List<String> buffer;
        private final GroovyBufferRunner bufferRunner;

        RunTask(GroovyBufferRunner bufferRunner, List<String> buffer){
            this.buffer = buffer;
            this.bufferRunner = bufferRunner;
        }

        @Override
        public void run() {
            bufferRunner.run(buffer);
        }
    }
}
