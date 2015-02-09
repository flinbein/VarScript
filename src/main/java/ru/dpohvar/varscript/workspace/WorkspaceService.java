package ru.dpohvar.varscript.workspace;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.workspace.decorate.*;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkspaceService {

    private static boolean usePlayerUniqueId;
    private static boolean useEntityUniqueId;

    static {
        try {
            OfflinePlayer.class.getMethod("getUniqueId");
            usePlayerUniqueId = true;
        } catch (NoSuchMethodException e) {
            usePlayerUniqueId = false;
        }
        try {
            Entity.class.getMethod("getUniqueId");
            useEntityUniqueId = true;
        } catch (NoSuchMethodException e) {
            useEntityUniqueId = false;
        }
    }

    public static Object getSenderHashKey(CommandSender sender){
        if (sender instanceof OfflinePlayer && usePlayerUniqueId) {
            return ((OfflinePlayer) sender).getUniqueId();
        } else if (sender instanceof Entity && useEntityUniqueId) {
            return ((Entity) sender).getUniqueId();
        } else if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock();
        }
        return sender.getName();
    }

    private VarScript plugin;
    private final File autorunDirectory;
    private final File scriptsDirectory;
    private final Map<String,Workspace> workspaceMap = new HashMap<String, Workspace>();
    private final List<CompilationCustomizer> compilationCustomizers = new LinkedList<CompilationCustomizer>();
    private final List<String> classPath = new LinkedList<String>();
    private VariableContainer globalVariables = new VariableContainer();

    public WorkspaceService(VarScript plugin){
        this.plugin = plugin;
        this.autorunDirectory = new File(plugin.getDataFolder(), "autorun");
        this.scriptsDirectory = new File(plugin.getDataFolder(), "scripts");
        classPath.add(scriptsDirectory.toString());
        globalVariables.getHardSetters().put("workspace", new WorkspaceSetter(this));
        globalVariables.getHardGetters().put("me", new MeGetter());
        globalVariables.getHardDynamicMethods().add(new GroovyScriptMethod(scriptsDirectory));
        globalVariables.getSoftDynamicGetters().add(new PlayerNameGetter(plugin.getServer()));
        globalVariables.getSoftDynamicGetters().add(new WorldNameGetter(plugin.getServer()));
        globalVariables.getSoftDynamicGetters().add(new PluginNameGetter(plugin.getServer().getPluginManager()));
    }

    public VarScript getPlugin() {
        return plugin;
    }

    public File getScriptsDirectory() {
        return scriptsDirectory;
    }

    public VariableContainer getGlobalVariables() {
        return globalVariables;
    }

    public File getAutorunDirectory(){
        return autorunDirectory;
    }

    public List<CompilationCustomizer> getCompilationCustomizers() {
        return compilationCustomizers;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public String getWorkspaceName(CommandSender sender){
        FileConfiguration config = plugin.getConfig();
        String workspaceName = config.getString("workspace." + sender.getName());
        if (workspaceName != null) return workspaceName;
        else return sender.getName();
    }

    public void setWorkspaceName(CommandSender sender, String workspaceName){
        FileConfiguration config = plugin.getConfig();
        config.set("workspace."+sender.getName(), workspaceName);
        plugin.saveConfig();
    }

    public Workspace getWorkspace(String workspaceName) {
        return workspaceMap.get(workspaceName);
    }

    public Workspace getOrCreateWorkspace(String workspaceName) {
        Workspace workspace = workspaceMap.get(workspaceName);
        if (workspace != null) return workspace;
        workspace = new Workspace(this, workspaceName);
        workspaceMap.put(workspaceName, workspace);
        return workspace;
    }
}



























