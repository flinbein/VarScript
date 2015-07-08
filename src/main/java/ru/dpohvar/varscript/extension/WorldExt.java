package ru.dpohvar.varscript.extension;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.List;

public class WorldExt {

    public static boolean isCase(World self, Location val) {
        return val.getWorld().equals(self);
    }

    public static boolean isCase(World self, Entity val) {
        return val.getWorld().equals(self);
    }

    public static boolean isCase(World self, Block val) {
        return val.getWorld().equals(self);
    }

    public static Location getAt(World self, List<Double> pos){
        return new Location(self, pos.get(0), pos.get(1),pos.get(2));
    }

    public static Block call(World self, int x, int y, int z){
        return self.getBlockAt(x,y,z);
    }
}
