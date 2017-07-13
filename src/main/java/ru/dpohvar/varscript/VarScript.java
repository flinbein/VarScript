package ru.dpohvar.varscript;

import org.apache.ivy.Ivy;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dpohvar.varscript.boot.BootHelper;
import ru.dpohvar.varscript.boot.VarScriptClassLoader;
import ru.dpohvar.varscript.caller.CallerService;
import ru.dpohvar.varscript.command.GroovyCommandCompleter;
import ru.dpohvar.varscript.command.GroovyCommandExecutor;
import ru.dpohvar.varscript.command.WorkspaceCommandCompleter;
import ru.dpohvar.varscript.command.WorkspaceCommandExecutor;
import ru.dpohvar.varscript.service.VarScriptHook;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class VarScript extends JavaPlugin {

    public static final String pluginName;
    public static final File pluginsFolder;
    public static final File dataFolder;
    public static final VarScriptClassLoader libLoader;
    public static final Ivy ivy;

    public static final String prefix = translateAlternateColorCodes('&',"&2&l[&a%s&2&l]>&r ");
    public static final String printPrefix = translateAlternateColorCodes('&',"&6&l[&e%s&6&l]>&r ");
    public static final String errorPrefix = translateAlternateColorCodes('&',"&4&l[&c%s&4&l]>&r ");
    public static final String promptLinePrefix = translateAlternateColorCodes('&',"&8&l[&7:%02d&8&l]>&r ");
    static {
        pluginName = "VarScript";
        pluginsFolder = new File("plugins");
        dataFolder = new File(pluginsFolder, pluginName);
        libLoader = new VarScriptClassLoader((URLClassLoader)VarScript.class.getClassLoader());
        BootHelper.prepareSystemVariables();
        BootHelper.loadLibraries();
        ivy = BootHelper.prepareIvy();
        BootHelper.loadSelfDependencies();
        BootHelper.configureGrape();
        BootHelper.loadAllPluginsIvyDependencies();
    }

    public boolean isDebug(){
        return getConfig().getBoolean("debug",false);
    }

    public void setDebug(boolean value){
        getConfig().set("debug",value);
        saveConfig();
    }


    private CallerService callerService;

    private WorkspaceService workspaceService;
    private ServiceLoader<VarScriptHook> serviceLoader;
    private GroovyCommandCompleter commandCompleter;

    public CallerService getCallerService() {
        return callerService;
    }

    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void onEnable() {
        callerService = new CallerService(this);
        workspaceService = new WorkspaceService(this);
        serviceLoader = ServiceLoader.load(VarScriptHook.class, libLoader);
        commandCompleter = new GroovyCommandCompleter(this);
        getCommand("script").setExecutor(new GroovyCommandExecutor(this));
        getCommand("script").setTabCompleter(commandCompleter);
        getCommand("workspace").setExecutor(new WorkspaceCommandExecutor(this));
        getCommand("workspace").setTabCompleter(new WorkspaceCommandCompleter(this));
        for (VarScriptHook provider : serviceLoader) {
            getLogger().info("load provider: "+provider);
            provider.onEnable(this);
        }
        workspaceService.startAutorun();
    }

    public GroovyCommandCompleter getCommandCompleter() {
        return commandCompleter;
    }

    @Override
    public void onDisable() {
        for (VarScriptHook provider : serviceLoader) {
            provider.onDisable(this);
        }
        for (Workspace workspace : workspaceService.getWorkspaces()) {
            workspace.removeWorkspace();
        }
    }
}
