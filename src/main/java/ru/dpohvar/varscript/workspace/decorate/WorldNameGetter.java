package ru.dpohvar.varscript.workspace.decorate;

import org.bukkit.Server;
import org.bukkit.World;
import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.VariableGetter;

public class WorldNameGetter implements VariableGetter {

    private final Server server;

    public WorldNameGetter(Server server){
        this.server = server;
    }

    @Override
    public Object getValue(String name, VariableContainer current, VariableContainer requester) {
        World world = server.getWorld(name);
        if (world == null) return SKIP_GETTER;
        return world;
    }
}
