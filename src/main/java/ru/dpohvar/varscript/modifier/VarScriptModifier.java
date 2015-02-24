package ru.dpohvar.varscript.modifier;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.utils.PropertySelector;
import ru.dpohvar.varscript.workspace.CallerScript;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class VarScriptModifier extends GroovyObjectSupport {

    private final File scriptDirectory;
    private final Server server;

    public VarScriptModifier(VarScript varScript) {
        this.server = varScript.getServer();
        this.scriptDirectory = varScript.getWorkspaceService().getScriptsDirectory();
    }

    public Object getPropertyFor(CallerScript script, String property) throws Exception {
        Caller caller = script.getCaller();
        try {
            return getMetaClass().invokeMethod(this,property,script);
        }
        catch (MissingMethodException ignored) {}
        catch (PropertySelector ignored) {}
        File scriptFile = new File(scriptDirectory, property + ".groovy");
        if (scriptFile.isFile()) return script.runFileScript(scriptFile);
        Player player = server.getPlayerExact(property);
        if (player != null) return player;
        Plugin plugin = server.getPluginManager().getPlugin(property);
        if (plugin != null) return plugin;
        World world = server.getWorld(property);
        if (world != null) return world;
        throw PropertySelector.next;
    }

    public Object invokeMethodFor(CallerScript script, String name, Object[] args) throws Exception {
        File scriptFile = new File(scriptDirectory, name + ".groovy");
        if (scriptFile.isFile()) return script.runFileScript(scriptFile, args);
        throw PropertySelector.next;
    }

    public Block block(CallerScript callerScript) throws PropertySelector {
        CommandSender sender = callerScript.getCaller().getSender();
        if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock();
        } else if (sender instanceof LivingEntity) {
            Block block = ((LivingEntity) sender).getTargetBlock((HashSet<Byte>)null, 128);
            if (block != null && !block.isEmpty()) return block;
        }
        throw PropertySelector.next;
    }

    public Location here(CallerScript callerScript) throws PropertySelector {
        Object me = callerScript.getMe();
        try {
            return (Location) InvokerHelper.getProperty(me, "location");
        } catch (Exception ignored){}
        throw PropertySelector.next;
    }

}
