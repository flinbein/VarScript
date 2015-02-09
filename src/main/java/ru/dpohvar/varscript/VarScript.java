package ru.dpohvar.varscript;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import ru.dpohvar.varscript.boot.BootHelper;
import ru.dpohvar.varscript.caller.CallerService;
import ru.dpohvar.varscript.command.GroovyCommandExecutor;
import ru.dpohvar.varscript.command.IvyCommandExecutor;
import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.util.Map;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class VarScript extends JavaPlugin {

    public static final String pluginName;
    public static final File pluginsFolder;
    public static final File dataFolder;
    public static final File targetFolder;
    public static final ClassLoader pluginClassLoader;

    public static final String prefix = translateAlternateColorCodes('&',"&2&l[&a>&2&l]&r ");
    public static final String printPrefix = translateAlternateColorCodes('&',"&6&l[&e>&6&l]&r ");
    public static final String errorPrefix = translateAlternateColorCodes('&',"&4&l[&c>&4&l]&r ");
    public static final String promptLinePrefix = translateAlternateColorCodes('&',"&8&l[&7%02d>&8&l]&r ");


    static {
        pluginName = "VarScript";
        pluginsFolder = new File("plugins");
        dataFolder = new File(pluginsFolder, pluginName);
        pluginClassLoader = VarScript.class.getClassLoader();
        BootHelper.prepareIvy();
        BootHelper.loadSelfDependencies();
        BootHelper.configureGrape();
        BootHelper.checkPlugins();
        targetFolder = new File(dataFolder, "target");
        targetFolder.mkdirs();
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

        VariableContainer globalVariables = workspaceService.getGlobalVariables();
        Map<String, Object> hardVariables = globalVariables.getHardVariables();
        hardVariables.put("server", getServer());

        CompilerConfiguration cc = new CompilerConfiguration();
        ImportCustomizer importCustomizer = new ImportCustomizer();
        cc.addCompilationCustomizers(importCustomizer);
        cc.setTargetDirectory(targetFolder);

        getCommand(">").setExecutor(new GroovyCommandExecutor(this));
        getCommand("ivy").setExecutor(new IvyCommandExecutor(this));
    }
}
