package ru.dpohvar.varscript.extension;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

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

    public static Location getAt(World self, List pos){
        Object p0 = DefaultGroovyMethods.getAt(pos, 0);
        Object p1 = DefaultGroovyMethods.getAt(pos, 1);
        Object p2 = DefaultGroovyMethods.getAt(pos, 2);
        Double x = DefaultGroovyMethods.asType(p0, double.class);
        Double y = DefaultGroovyMethods.asType(p1, double.class);
        Double z = DefaultGroovyMethods.asType(p2, double.class);
        return new Location(self, x, y, z);
    }

    public static Block call(World self, int x, int y, int z){
        return self.getBlockAt(x,y,z);
    }
}
