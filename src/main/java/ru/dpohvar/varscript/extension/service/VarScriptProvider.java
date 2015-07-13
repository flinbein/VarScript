package ru.dpohvar.varscript.extension.service;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.extension.completer.EntityIdCompleter;
import ru.dpohvar.varscript.service.VarScriptHook;
import ru.dpohvar.varscript.utils.PropertySelector;
import ru.dpohvar.varscript.utils.ScriptProperties;
import ru.dpohvar.varscript.workspace.CallerScript;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

public class VarScriptProvider extends GroovyObjectSupport implements VarScriptHook {

    private File scriptDirectory;
    private Server server;

    @Override
    public void onEnable(VarScript plugin) {
        WorkspaceService service = plugin.getWorkspaceService();
        scriptDirectory = service.getScriptsDirectory();
        server = plugin.getServer();
        plugin.getCommandCompleter().getDelegateCompleters().add(new EntityIdCompleter());
        CallerScript.getDynamicModifiers().add(this);
        //// this customizers is overrided by classloader
        //ImportVersionASTTransformation astTransformation = new ImportVersionASTTransformation();
        //CompilationCustomizer nmsCustomizer = new SourceASTTransformationCustomizer(astTransformation);
        //service.getCompilationCustomizers().add(nmsCustomizer);
    }

    @Override
    public void onDisable(VarScript plugin) {
    }

    public Map<String,Class> getPropertyMapFor(ScriptProperties script){
        Map<String,Class> result = new HashMap<String, Class>();
        for (World world : server.getWorlds()) {
            result.put(world.getName(), world.getClass());
        }
        for (Plugin plugin : server.getPluginManager().getPlugins()) {
            result.put(plugin.getName(), plugin.getClass());
        }
        for (Player player : server.getOnlinePlayers()) {
            result.put(player.getName(), player.getClass());
        }
        File[] files = scriptDirectory.listFiles();
        if (files != null) for (File file : files) {
            String name = file.getName();
            if (!name.endsWith(".groovy")) continue;
            if (name.length() <= 7) continue;
            result.put(name.substring(0, name.length()-7), null);
        }
        for (Method method : this.getClass().getMethods()) {
            Class<?>[] types = method.getParameterTypes();
            if (types.length == 1 && types[0].equals(ScriptProperties.class)) {
                result.put(method.getName(), method.getReturnType());
            }
        }
        return result;
    }

    public Map<String,Class> getMethodMapFor(ScriptProperties script){
        Map<String,Class> result = new HashMap<String, Class>();
        File[] files = scriptDirectory.listFiles();
        if (files != null) for (File file : files) {
            String name = file.getName();
            if (!name.endsWith(".groovy")) continue;
            if (name.length() <= 7) continue;
            result.put(name.substring(0, name.length()-7), null);
        }
        return result;
    }

    public Object getPropertyFor(ScriptProperties script, String property) throws Exception {
        Caller caller = script.getCaller();
        try {
            return getMetaClass().invokeMethod(this,property,script);
        }
        catch (MissingMethodException ignored) {}
        catch (PropertySelector ignored) {}
        if (script instanceof CallerScript) {
            File scriptFile = new File(scriptDirectory, property + ".groovy");
            if (scriptFile.isFile()) return ((CallerScript) script).runScriptFile(scriptFile);
        }
        Player player = server.getPlayerExact(property);
        if (player != null) return player;
        Plugin plugin = server.getPluginManager().getPlugin(property);
        if (plugin != null) return plugin;
        World world = server.getWorld(property);
        if (world != null) return world;
        throw PropertySelector.next;
    }

    public Object invokeMethodFor(ScriptProperties script, String name, Object[] args) throws Exception {
        if (script instanceof CallerScript) {
            File scriptFile = new File(scriptDirectory, name + ".groovy");
            if (scriptFile.isFile()) return ((CallerScript) script).runScriptFile(scriptFile, args);
        }
        throw PropertySelector.next;
    }

    public Block block(ScriptProperties callerScript) throws PropertySelector {
        CommandSender sender = callerScript.getCaller().getSender();
        if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock();
        } else if (sender instanceof LivingEntity) {
            Block block = ((LivingEntity) sender).getTargetBlock((HashSet<Byte>)null, 128);
            if (block != null && !block.isEmpty()) return block;
        }
        throw PropertySelector.next;
    }

    public Location here(ScriptProperties callerScript) throws PropertySelector {
        Object me = callerScript.getMe();
        try {
            return (Location) InvokerHelper.getProperty(me, "location");
        } catch (Exception ignored){}
        throw PropertySelector.next;
    }

    public List<Entity> entities(ScriptProperties callerScript) throws PropertySelector {
        List<Entity> result = new ArrayList<Entity>();
        for (World world : server.getWorlds()) {
            result.addAll( world.getEntities() );
        }
        return result;
    }

    public Collection<? extends Player> players(ScriptProperties callerScript) throws PropertySelector {
        return server.getOnlinePlayers();
    }

    public List<Item> items(ScriptProperties callerScript) throws PropertySelector {
        List<Item> result = new ArrayList<Item>();
        for (World world : server.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) result.add((Item)entity);
            }
        }
        return result;
    }

    public List<World> worlds(ScriptProperties callerScript) throws PropertySelector {
        return server.getWorlds();
    }

}
