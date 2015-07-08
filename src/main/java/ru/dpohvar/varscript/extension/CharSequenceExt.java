package ru.dpohvar.varscript.extension;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
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

public class CharSequenceExt {

    // getters

    public static String getColor(CharSequence self) {
        return ChatColor.translateAlternateColorCodes('&', self.toString());
    }

    public static String getC(CharSequence self) {
        return getColor(self);
    }

    public static String getStripColor(CharSequence self) {
        return ChatColor.stripColor(self.toString());
    }

    public static String getSc(CharSequence self) {
        return getStripColor(self);
    }

    public static String getLastColors(CharSequence self) {
        return ChatColor.getLastColors(self.toString());
    }

    public static String getLc(CharSequence self) {
        return getLastColors(self);
    }

    public static Player getPlayer(CharSequence self) {
        return Bukkit.getPlayer(self.toString().trim());
    }

    public static Player getP(CharSequence self) {
        return getPlayer(self);
    }

    public static World getWorld(CharSequence self) {
        return Bukkit.getWorld(self.toString().trim());
    }

    public static World getW(CharSequence self) {
        return getWorld(self);
    }

    public static OfflinePlayer getOfflinePlayer(CharSequence self) {
        return Bukkit.getOfflinePlayer(self.toString().trim());
    }

    public static OfflinePlayer getOfp(CharSequence self) {
        return getOfflinePlayer(self);
    }

    public static char getChar(CharSequence self) {
        return self.charAt(0);
    }

    public static String getUp(CharSequence self) {
        return self.toString().toUpperCase();
    }

    public static String getLow(CharSequence self) {
        return self.toString().toLowerCase();
    }

    public static CharSequence rightShift(CharSequence self, CommandSender val) {
        val.sendMessage(self.toString());
        return self;
    }

}
