package ru.dpohvar.varscript.extension.completer;

import com.google.common.base.Strings;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import static java.util.Comparator.comparingDouble;
import java.util.List;
import java.util.TreeSet;

public class EntityIdCompleter implements TabCompleter{

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {

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
            List<String> result = new ArrayList<>();
            TreeSet<Entity> entities = new TreeSet<>(comparingDouble(e -> e.getLocation().distance(location)));
            entities.addAll( location.getWorld().getEntities() );
            var pow = (int) Math.ceil(Math.log10(entities.size()));
            var i = 0;
            for (Entity entity : entities) {
                var prefix = pow == 0 ? "" : Strings.padStart(String.valueOf(i), pow, ' ')+":";
                if (entity == commandSender) continue;
                result.add("id(/*" + prefix + entity.getName() + "*/" + entity.getEntityId() + ")");
                i++;
            }
            return result;
        } else {
            return null;
        }

    }
}
