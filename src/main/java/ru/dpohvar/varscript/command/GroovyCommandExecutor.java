package ru.dpohvar.varscript.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.plugin.PluginManager;
import org.codehaus.groovy.tools.shell.ParseCode;
import org.codehaus.groovy.tools.shell.ParseStatus;
import org.codehaus.groovy.tools.shell.Parser;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.event.EnterScriptLineEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroovyCommandExecutor implements CommandExecutor{

    static final int COMPLETE = ParseCode.getCOMPLETE().getCode();
    static final int ERROR = ParseCode.getERROR().getCode();
    static final int INCOMPLETE = ParseCode.getINCOMPLETE().getCode();

    private final VarScript plugin;
    private final PluginManager pluginManager;
    private final Parser parser = new Parser();
    private final ConversationFactory factory;

    public GroovyCommandExecutor(VarScript plugin){
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();
        factory = new ConversationFactory(plugin)
                .withFirstPrompt(new GroovyLinePrompt(pluginManager,parser))
                .withLocalEcho(false)
                .withTimeout(300);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String inputLine = StringUtils.join(strings, ' ');
        if (commandSender instanceof Conversable && ((Conversable) commandSender).isConversing()) {
            ((Conversable) commandSender).acceptConversationInput(inputLine);
            return true;
        }

        List<String> buffer = new ArrayList<String>();
        GroovyLinePrompt.LineInputAction inputAction = GroovyLinePrompt.LineInputAction.INPUT;
        EnterScriptLineEvent enterScriptLineEvent = new EnterScriptLineEvent(commandSender, inputLine, buffer, inputAction);
        pluginManager.callEvent(enterScriptLineEvent);
        if (enterScriptLineEvent.isCancelled()) return true;
        inputAction = enterScriptLineEvent.getInputAction();
        if (inputAction == null) return true;
        inputLine = enterScriptLineEvent.getLine();

        Caller caller = plugin.getCallerService().getCaller(commandSender);
        GroovyBufferRunner runner = new GroovyBufferRunner(caller);

        switch (inputAction) {
            case INPUT:
                if (inputLine != null) buffer.add(inputLine);
                break;
            case UP:
                if (buffer.size() > 1) buffer.remove(buffer.size()-1);
                break;
            case CANCEL:
                return true;
            case RUN:
                runner.compileAsyncAndRun(buffer);
                return true;
        }

        ParseStatus status = parser.parse(buffer);
        int code = status.getCode().getCode();
        if (code == INCOMPLETE) {
            if (commandSender instanceof Conversable) {
                Map<Object,Object> sessionData = new HashMap<Object,Object>();
                sessionData.put("input", inputLine);
                sessionData.put("buffer", buffer);
                sessionData.put("runner", runner);

                Conversation conversation = factory
                        .withInitialSessionData(sessionData)
                        .buildConversation((Conversable) commandSender);
                conversation.begin();
                return true;
            } else {
                return false;
                // ERROR YOU ARE BLOCK! MWAHAHAHAHA
            }
        } else {
            runner.compileAsyncAndRun(buffer);
        }
        return true;
    }
}
