package ru.dpohvar.varscript.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkspaceCommandCompleter implements TabCompleter {

    static final List<String> commands = Arrays.asList("list", "set", "reload", "remove", "create", "stop");
    private final VarScript plugin;

    public WorkspaceCommandCompleter(VarScript plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) return commands;
        if (strings.length == 1) {
            List<String> result = new ArrayList<String>();
            String pattern = strings[0];
            for (String cmd : commands) {
                if (cmd.startsWith(pattern)) result.add(cmd);
            }
            return result;
        }
        String cmd = strings[0];
        if (strings.length == 2) {
            List<String> result = new ArrayList<String>();
            String pattern = strings[1];
            WorkspaceService service = plugin.getWorkspaceService();
            if (cmd.equals("reload")||cmd.equals("remove")||cmd.equals("stop")||cmd.equals("set")) {
                for (Workspace workspace : service.getWorkspaces()) {
                    String name = workspace.getName();
                    if (name.startsWith(pattern)) result.add(name);
                }
            }
            if (cmd.equals("reload")||cmd.equals("create")) {
                File[] files = service.getAutorunDirectory().listFiles();
                if (files != null) for (File file : files) {
                    String fileName = file.getName();
                    if (!fileName.endsWith(".groovy")||fileName.length()<8) continue;
                    String name = fileName.substring(0, fileName.length()-7);
                    if (!result.contains(name)) result.add(name);
                }
            }
            return result;
        }
        return null;
    }
}
