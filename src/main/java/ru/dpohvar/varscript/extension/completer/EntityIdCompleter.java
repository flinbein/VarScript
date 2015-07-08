package ru.dpohvar.varscript.extension.completer;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class EntityIdCompleter implements TabCompleter{

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        String expression = strings[strings.length-1];
        if (!expression.equals("id") && !expression.equals("id(")) return null;

        final Location location;
        if (commandSender instanceof Entity) {
            location = ((Entity) commandSender).getLocation();
        } else if (commandSender instanceof BlockCommandSender) {
            location = ((BlockCommandSender) commandSender).getBlock().getLocation();
        } else {
            location = null;
        }
        if (location != null) {
            List<String> result = new ArrayList<String>();
            TreeSet<Entity> entities = new TreeSet<Entity>(new Comparator<Entity>() {
                @Override
                public int compare(Entity o1, Entity o2) {
                    double d1 =  o1.getLocation().distance(location);
                    double d2 =  o2.getLocation().distance(location);
                    if (d1 < d2) return -1;
                    else if (d1 > d2) return 1;
                    return 0;
                }
            });
            entities.addAll( location.getWorld().getEntities() );
            for (Entity entity : entities) {
                if (entity == commandSender) continue;
                result.add("id("+entity.getEntityId()+"/*"+entity.getType()+"*/)");
            }
            return result;
        } else {
            return null;
        }

    }
}
