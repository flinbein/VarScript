package ru.dpohvar.varscript.caller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.workspace.WorkspaceService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CallerService implements Listener {

    private final VarScript plugin;
    private Map<Object, Caller> callerMap = new HashMap<Object, Caller>();

    public CallerService(VarScript plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public VarScript getPlugin() {
        return plugin;
    }

    @EventHandler()
    public void playerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Caller caller = callerMap.get(uuid);
        caller.sender = player;
    }

    public Caller getCaller(CommandSender sender){
        if (sender == null) return null;
        Object hash = WorkspaceService.getSenderHashKey(sender);
        Caller caller = callerMap.get(hash);
        if (caller == null) {
            caller = createCaller(sender);
            callerMap.put(hash, caller);
        }
        return caller;
    }

    private Caller createCaller(CommandSender sender){
        return new Caller(this, sender);
    }

}
