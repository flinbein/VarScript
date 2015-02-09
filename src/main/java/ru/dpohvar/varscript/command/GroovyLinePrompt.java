package ru.dpohvar.varscript.command;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.codehaus.groovy.tools.shell.ParseStatus;
import org.codehaus.groovy.tools.shell.Parser;
import ru.dpohvar.varscript.VarScript;

import java.util.List;

import static org.bukkit.ChatColor.translateAlternateColorCodes;
import static ru.dpohvar.varscript.command.GroovyCommandExecutor.*;

public class GroovyLinePrompt extends StringPrompt{
    private final Parser parser;

    public GroovyLinePrompt(Parser parser){
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
    public Prompt acceptInput(ConversationContext conversationContext, String s) {

        if (s.equals("\\clear")) {
            return Prompt.END_OF_CONVERSATION;
        }
        List buffer = (List) conversationContext.getSessionData("buffer");
        if (s.equals("\\up")) {
            if (!buffer.isEmpty()) buffer.remove(buffer.size()-1);
            return this;
        }
        if (s.equals("\\run")) {
            GroovyBufferRunner runner = (GroovyBufferRunner) conversationContext.getSessionData("runner");
            runner.runNextTick(buffer);
            return Prompt.END_OF_CONVERSATION;
        }

        buffer.add(s);
        ParseStatus status = parser.parse(buffer);

        int code = status.getCode().getCode();
        if (code == INCOMPLETE) {
            return this;
        } else if (code == ERROR){
            //noinspection ThrowableResultOfMethodCallIgnored
            conversationContext.getForWhom().sendRawMessage(VarScript.errorPrefix + status.getCause().getMessage());
            buffer.remove(buffer.size()-1);
            return this;
        } else if (code == COMPLETE) {
            String promptText = getPromptText(conversationContext);
            conversationContext.getForWhom().sendRawMessage(promptText);
            GroovyBufferRunner runner = (GroovyBufferRunner) conversationContext.getSessionData("runner");
            runner.runNextTick(buffer);
            return Prompt.END_OF_CONVERSATION;
        }
        return this;
    }
}
