package ru.dpohvar.varscript.extension;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class WorldStaticExt {

    public static World load(World ignored, String val) {
        return Bukkit.getServer().createWorld(new WorldCreator(val));
    }
}
