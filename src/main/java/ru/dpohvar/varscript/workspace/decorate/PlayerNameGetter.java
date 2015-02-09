package ru.dpohvar.varscript.workspace.decorate;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.VariableGetter;

public class PlayerNameGetter implements VariableGetter {

    private final Server server;

    public PlayerNameGetter(Server server){
        this.server = server;
    }

    @Override
    public Object getValue(String name, VariableContainer current, VariableContainer requester) {
        Player player = server.getPlayerExact(name);
        if (player == null) return SKIP_GETTER;
        return player;
    }
}
