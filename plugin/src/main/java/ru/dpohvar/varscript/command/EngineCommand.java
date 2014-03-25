package ru.dpohvar.varscript.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.VarScriptPlugin;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Iterator;

import static org.bukkit.ChatColor.RESET;
import static org.bukkit.ChatColor.*;

/**
 * Created by DPOH-VAR on 24.02.14
 */

public class EngineCommand implements CommandExecutor {

    private VarScriptPlugin plugin;

    public EngineCommand(VarScriptPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("sctipt engines:");
            for (ScriptEngine engine: plugin.getScriptEngines()) {
                ScriptEngineFactory factory = engine.getFactory();
                builder.append("\n").append(GREEN);
                builder.append(factory.getLanguageName());
                builder.append(" ").append(YELLOW);
                builder.append(factory.getEngineName());
                builder.append(" ").append(WHITE);
                builder.append(factory.getEngineVersion());
            }
            RunCodeCommand.sendResult(commandSender, builder);
        } else if (strings.length == 1) {
            ScriptEngine engine = plugin.getScriptEngineByName(strings[0]);
            if (engine == null) {
                RunCodeCommand.sendError(commandSender, "Engine "+YELLOW+strings[0]+RESET+" not found");
            } else {
                StringBuilder builder = new StringBuilder();
                ScriptEngineFactory factory = engine.getFactory();
                builder.append(YELLOW).append(factory.getEngineName()).append(' ');
                builder.append(RESET).append(factory.getEngineVersion()).append('\n');
                builder.append("Language: ");
                builder.append(YELLOW).append(factory.getLanguageName()).append(' ');
                builder.append(RESET).append(factory.getLanguageVersion());
                builder.append('\n').append("Names: ");
                Iterator<String> nameItr = factory.getNames().iterator();
                if (nameItr.hasNext()) for (;;) {
                    String name = nameItr.next();
                    builder.append(YELLOW);
                    builder.append(name);
                    builder.append(RESET);
                    if (!nameItr.hasNext()) break;
                    builder.append(", ");
                }
                builder.append("\n").append("Extensions: ");
                Iterator<String> extItr = factory.getExtensions().iterator();
                if (extItr.hasNext()) for (;;) {
                    String name = extItr.next();
                    builder.append(YELLOW);
                    builder.append(name);
                    builder.append(RESET);
                    if (!extItr.hasNext()) break;
                    builder.append(", ");
                }
                builder.append("\n").append("MIME Types: ");
                Iterator<String> mimeItr = factory.getMimeTypes().iterator();
                if (mimeItr.hasNext()) for (;;) {
                    String name = mimeItr.next();
                    builder.append(YELLOW);
                    builder.append(name);
                    builder.append(RESET);
                    if (!mimeItr.hasNext()) break;
                    builder.append(", ");
                }
                RunCodeCommand.sendResult(commandSender, builder);
            }
        } else {
            return false;
        }
        return true;
    }

}
