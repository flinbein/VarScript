package ru.dpohvar.varscript.extension;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class WorldStaticExt {

    public static World load(World ignored, String val) {
        return Bukkit.getServer().createWorld(new WorldCreator(val));
    }
}
