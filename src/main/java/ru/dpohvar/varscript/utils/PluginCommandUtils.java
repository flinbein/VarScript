package ru.dpohvar.varscript.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.util.Map;

import static ru.dpohvar.varscript.utils.ReflectionUtils.*;

public class PluginCommandUtils {

    static ReflectionUtils.RefClass<PluginCommand> cPluginCommand = getRefClass(PluginCommand.class);
    static RefConstructor<PluginCommand> nPluginCommand = cPluginCommand.getConstructor(String.class, Plugin.class);
    static RefClass<?> cCraftServer = getRefClass("{cb}.CraftServer");
    static RefMethod<SimpleCommandMap> getCommandMap = cCraftServer.findMethodByReturnType(SimpleCommandMap.class);
    static SimpleCommandMap commandMap = getCommandMap.of(Bukkit.getServer()).call();
    static RefField fKnownCommands = getRefClass(SimpleCommandMap.class).getField("knownCommands");

    static Map getKnownCommands() {
        return (Map) fKnownCommands.of(commandMap).get();
    }
}
