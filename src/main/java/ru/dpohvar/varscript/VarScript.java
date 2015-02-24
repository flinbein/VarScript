package ru.dpohvar.varscript;

import org.apache.ivy.Ivy;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dpohvar.varscript.boot.BootHelper;
import ru.dpohvar.varscript.caller.CallerService;
import ru.dpohvar.varscript.command.GroovyCommandExecutor;
import ru.dpohvar.varscript.command.WorkspaceCommandCompleter;
import ru.dpohvar.varscript.command.WorkspaceCommandExecutor;
import ru.dpohvar.varscript.modifier.VarScriptModifier;
import ru.dpohvar.varscript.service.VarScriptServiceProvider;
import ru.dpohvar.varscript.workspace.CallerScript;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.util.ServiceLoader;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class VarScript extends JavaPlugin {

    public static final String pluginName;
    public static final File pluginsFolder;
    public static final File dataFolder;
    public static final ClassLoader pluginClassLoader;
    public static final Ivy ivy;

    public static final String prefix = translateAlternateColorCodes('&',"&2&l[&a%s>&2&l]&r ");
    public static final String printPrefix = translateAlternateColorCodes('&',"&6&l[&e%s>&6&l]&r ");
    public static final String errorPrefix = translateAlternateColorCodes('&',"&4&l[&c%s>&4&l]&r ");
    public static final String promptLinePrefix = translateAlternateColorCodes('&',"&8&l[&7%02d>&8&l]&r ");


    static {
        pluginName = "VarScript";
        pluginsFolder = new File("plugins");
        dataFolder = new File(pluginsFolder, pluginName);
        pluginClassLoader = VarScript.class.getClassLoader();
        BootHelper.loadLibraries();
        ivy = BootHelper.prepareIvy();
        BootHelper.loadSelfDependencies();
        BootHelper.configureGrape();
        // BootHelper.loadExtensions(pluginClassLoader);
        // BootHelper.checkPlugins();
    }

    private CallerService callerService;
    private WorkspaceService workspaceService;

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
        CallerScript.getDynamicModifiers().add(new VarScriptModifier(this));
        for (VarScriptServiceProvider provider : ServiceLoader.load(VarScriptServiceProvider.class)) {
            provider.onEnable(this);
        }
        workspaceService.startAutorun();
        getCommand(">").setExecutor(new GroovyCommandExecutor(this));
        getCommand("workspace").setExecutor(new WorkspaceCommandExecutor(this));
        getCommand("workspace").setTabCompleter(new WorkspaceCommandCompleter(this));
    }

    @Override
    public void onDisable() {
        for (VarScriptServiceProvider provider : ServiceLoader.load(VarScriptServiceProvider.class)) {
            provider.onDisable(this);
        }
        for (Workspace workspace : workspaceService.getWorkspaces()) {
            workspace.removeWorkspace();
        }
    }
}
