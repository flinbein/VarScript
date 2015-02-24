package ru.dpohvar.varscript.extension;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.dpohvar.varscript.extension.region.BoxArea;
import ru.dpohvar.varscript.extension.region.BoxRegion;
import ru.dpohvar.varscript.extension.region.SphereArea;
import ru.dpohvar.varscript.extension.region.SphereRegion;

import java.util.Iterator;
import java.util.List;

public class StringExt {

    // getters

    public static String getColor(String self) {
        return ChatColor.translateAlternateColorCodes('&', self);
    }

    public static String getC(String self) {
        return getColor(self);
    }

    public static String getStripColor(String self) {
        return ChatColor.stripColor(self);
    }

    public static String getSc(String self) {
        return getStripColor(self);
    }

    public static String getLastColors(String self) {
        return ChatColor.getLastColors(self);
    }

    public static String getLc(String self) {
        return getLastColors(self);
    }

    public static Player getPlayer(String self) {
        return Bukkit.getPlayer(self.trim());
    }

    public static Player getP(String self) {
        return getPlayer(self);
    }

    public static OfflinePlayer getOfflinePlayer(String self) {
        return Bukkit.getOfflinePlayer(self.trim());
    }

    public static OfflinePlayer getOfp(String self) {
        return getOfflinePlayer(self);
    }

    public static char getChar(String self) {
        return self.charAt(0);
    }

    public static String getUp(String self) {
        return self.toUpperCase();
    }

    public static String getLowe(String self) {
        return self.toLowerCase();
    }

}
