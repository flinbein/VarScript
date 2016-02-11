package ru.dpohvar.varscript.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.plugin.PluginManager;
import org.codehaus.groovy.tools.shell.ParseStatus;
import org.codehaus.groovy.tools.shell.Parser;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.event.EnterScriptLineEvent;

import java.util.List;

import static org.bukkit.ChatColor.translateAlternateColorCodes;
import static ru.dpohvar.varscript.command.GroovyCommandExecutor.*;

public class GroovyLinePrompt extends StringPrompt{
    private final Parser parser;
    private final PluginManager pluginManager;

    public GroovyLinePrompt(PluginManager pluginManager, Parser parser){
        this.pluginManager = pluginManager;
        this.parser = parser;
    }

    @Override
    public String getPromptText(ConversationContext conversationContext) {
        List buffer = (List) conversationContext.getSessionData("buffer");
        String prefix = String.format(VarScript.promptLinePrefix, buffer.size());
        if (buffer.size() == 0) return prefix + translateAlternateColorCodes('&', "&7<no lines>&r");
        return prefix + buffer.get(buffer.size()-1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Prompt acceptInput(ConversationContext conversationContext, String inputLine) {

        LineInputAction inputAction = LineInputAction.INPUT;
        if (inputLine.equals("\\clear")||inputLine.equals("\\cancel")) inputAction = LineInputAction.CANCEL;
        if (inputLine.equals("\\up")) inputAction = LineInputAction.UP;
        if (inputLine.equals("\\run")) inputAction = LineInputAction.RUN;
        CommandSender sender = (CommandSender) conversationContext.getForWhom();
        List buffer = (List) conversationContext.getSessionData("buffer");

        EnterScriptLineEvent enterScriptLineEvent = new EnterScriptLineEvent(sender, inputLine, buffer, inputAction);
        pluginManager.callEvent(enterScriptLineEvent);
        inputAction = enterScriptLineEvent.getInputAction();
        if (enterScriptLineEvent.isCancelled() || inputAction == null) {
            return this;
        }
        inputLine = enterScriptLineEvent.getLine();

        switch (inputAction) {
            case UP:
                if (!buffer.isEmpty()) buffer.remove(buffer.size()-1);
                return this;
            case CANCEL:
                String prefix = String.format(VarScript.promptLinePrefix, buffer.size());
                conversationContext.getForWhom().sendRawMessage(prefix + ChatColor.YELLOW + "<cancelled>");
                return Prompt.END_OF_CONVERSATION;
            case RUN:
                GroovyBufferRunner runner = (GroovyBufferRunner) conversationContext.getSessionData("runner");
                runner.compileAsyncAndRun(buffer);
                return Prompt.END_OF_CONVERSATION;
        }

        buffer.add(inputLine);
        ParseStatus status = parser.parse(buffer);

        int code = status.getCode().getCode();
        if (code == INCOMPLETE) {
            return this;
        } else if (code == ERROR){
            String prefix = String.format(VarScript.promptLinePrefix, buffer.size());
            //noinspection ThrowableResultOfMethodCallIgnored
            conversationContext.getForWhom().sendRawMessage(prefix + ChatColor.RED + status.getCause().getMessage());
            buffer.remove(buffer.size()-1);
            return this;
        } else if (code == COMPLETE) {
            String promptText = getPromptText(conversationContext);
            conversationContext.getForWhom().sendRawMessage(promptText);
            GroovyBufferRunner runner = (GroovyBufferRunner) conversationContext.getSessionData("runner");
            runner.compileAsyncAndRun(buffer);
            return Prompt.END_OF_CONVERSATION;
        }
        return this;
    }

    public static enum LineInputAction {
        INPUT, CANCEL, UP, RUN
    }
}
