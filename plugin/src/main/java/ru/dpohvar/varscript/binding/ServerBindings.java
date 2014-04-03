package ru.dpohvar.varscript.binding;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import javax.script.Bindings;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerBindings implements Bindings {

    private final Server server;
    private final PluginManager manager;
    private final Map<String, Object> map = new HashMap<>();

    public ServerBindings() {
        this.server = Bukkit.getServer();
        this.manager = server.getPluginManager();
    }

    @Override
    public Object put(String name, Object value) {
        return map.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        map.putAll(toMerge);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) return false;
        Plugin plugin = manager.getPlugin((String) key);
        if (plugin != null) return true;
        if (map.containsKey(key)) return true;
        Player player = server.getPlayerExact((String) key);
        return player != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String)) return false;
        Plugin plugin = manager.getPlugin((String) key);
        if (plugin != null) return plugin;
        if (map.containsKey(key)) return map.get(key);
        return server.getPlayerExact((String) key);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }
}
