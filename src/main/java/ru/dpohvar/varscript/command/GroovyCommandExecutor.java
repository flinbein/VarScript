package ru.dpohvar.varscript.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.codehaus.groovy.tools.shell.ParseCode;
import org.codehaus.groovy.tools.shell.ParseStatus;
import org.codehaus.groovy.tools.shell.Parser;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroovyCommandExecutor implements CommandExecutor{

    static final int COMPLETE = ParseCode.getCOMPLETE().getCode();
    static final int ERROR = ParseCode.getERROR().getCode();
    static final int INCOMPLETE = ParseCode.getINCOMPLETE().getCode();

    private final VarScript plugin;
    private final Parser parser = new Parser();
    private final ConversationFactory factory;

    public GroovyCommandExecutor( VarScript plugin){
        this.plugin = plugin;
        factory = new ConversationFactory(plugin)
                .withFirstPrompt(new GroovyLinePrompt(parser))
                .withLocalEcho(false)
                .withTimeout(120);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String inputLine = StringUtils.join(strings, ' ');
        Map<Object,Object> sessionData = new HashMap<Object,Object>();
        sessionData.put("input", inputLine);
        List<String> buffer = new ArrayList<String>();
        buffer.add(inputLine);
        sessionData.put("buffer", buffer);
        Caller caller = plugin.getCallerService().getCaller(commandSender);
        GroovyBufferRunner runner = new GroovyBufferRunner(caller);
        sessionData.put("runner", runner);


        ParseStatus status = parser.parse(buffer);
        int code = status.getCode().getCode();
        if (code == INCOMPLETE) {
            if (commandSender instanceof Conversable) {
                Conversation conversation = factory
                        .withInitialSessionData(sessionData)
                        .buildConversation((Conversable) commandSender);
                conversation.begin();
                return true;
            } else {
                // ERROR YOU ARE BLOCK! HAHAHAHA
            }
        } else {
            runner.run(buffer);
        }
        return true;
    }
}
