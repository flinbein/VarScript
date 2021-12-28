package ru.dpohvar.varscript.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.scheduler.BukkitScheduler;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.event.CompileScriptEvent;
import ru.dpohvar.varscript.workspace.CallerScript;
import ru.dpohvar.varscript.workspace.Workspace;

import java.util.List;

public class GroovyBufferRunner {

    private final Caller caller;

    public GroovyBufferRunner(Caller caller){
        this.caller = caller;
    }

    public void compileAsyncAndRun(final List<String> buffer){
        String script = StringUtils.join(buffer, '\n');
        Caller caller = this.caller;
        Workspace workspace = caller.getCurrentWorkspace();

        final VarScript plugin = caller.getService().getPlugin();
        final BukkitScheduler scheduler = plugin.getServer().getScheduler();
        final CompileScriptEvent event = new CompileScriptEvent(caller, script, workspace);

        plugin.getServer().getPluginManager().callEvent(event);
        script = event.getScript();
        caller = event.getCaller();
        workspace = event.getWorkspace();

        if (event.isCancelled() || script == null || caller == null || workspace == null) return;
        AsyncCompiler asyncCompiler = new AsyncCompiler(scheduler, plugin, workspace, caller, script);
        scheduler.runTaskAsynchronously(plugin, asyncCompiler);
    }

    public Caller getCaller() {
        return caller;
    }

    private record AsyncCompiler(
            BukkitScheduler scheduler,
            VarScript plugin,
            Workspace workspace,
            Caller caller,
            String script
    ) implements Runnable {

        @Override
        public void run() {
            Object result = null;
            Exception compileException = null;
            try {
                result = workspace.compileScript(caller, script, null);
            } catch (Exception e) {
                compileException = e;
            }
            SyncExecutor syncExecutor = new SyncExecutor(result, caller, workspace, compileException);
            scheduler.runTask(plugin, syncExecutor);
        }
    }

    private static class SyncExecutor implements Runnable{

        private final Object compileResult;
        private final Caller caller;
        private final Workspace workspace;
        private final Exception exception;

        private SyncExecutor(Object compileResult, Caller caller, Workspace workspace, Exception exception) {
            this.compileResult = compileResult;
            this.caller = caller;
            this.workspace = workspace;
            this.exception = exception;
        }

        @Override
        public void run() {
            if (exception != null) {
                caller.sendThrowable(exception, workspace.getName());
            } else try {
                Object result = compileResult;
                if (result instanceof CallerScript) result = ((CallerScript) result).run();
                if (result != null) caller.sendMessage(DefaultGroovyMethods.toString(result), workspace.getName());
                caller.setLastResult(result);
            } catch (Throwable e) {
                caller.sendThrowable(e, workspace.getName());
            }
        }
    }

}
