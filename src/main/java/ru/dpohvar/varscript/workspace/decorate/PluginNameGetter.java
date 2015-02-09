package ru.dpohvar.varscript.workspace.decorate;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.VariableGetter;

public class PluginNameGetter implements VariableGetter {

    private final PluginManager pluginManager;

    public PluginNameGetter(PluginManager pluginManager){
        this.pluginManager = pluginManager;
    }

    @Override
    public Object getValue(String name, VariableContainer current, VariableContainer requester) {
        Plugin plugin = pluginManager.getPlugin(name);
        if (plugin == null) return SKIP_GETTER;
        return plugin;
    }
}
