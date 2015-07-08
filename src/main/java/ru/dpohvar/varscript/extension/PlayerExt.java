package ru.dpohvar.varscript.extension;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerExt {

    public static boolean isFly(Player self) {
        return self.getAllowFlight();
    }

    public static void setFly(Player self, boolean value) {
        self.setAllowFlight(value);
    }

    public static <T extends Player> T kick(T self, CharSequence reason) {
        self.kickPlayer(reason.toString());
        return self;
    }

    public static <T extends Player> T kick(T self) {
        return kick(self,"");
    }

    public static Location getBed(Player self) {
        return self.getBedSpawnLocation();
    }

    public static void setBed(Player self, Location value) {
        self.setBedSpawnLocation(value);
    }

    public static <T extends Player> T setBed(T self, Location value, boolean force) {
        self.setBedSpawnLocation(value, force);
        return self;
    }

}
