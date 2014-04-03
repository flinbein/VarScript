package ru.dpohvar.varscript.extension

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.craftbukkit.libs.com.google.gson.Gson
import org.bukkit.entity.Player
import org.yaml.snakeyaml.Yaml

/**
 * Created by DPOH-VAR on 06.03.14
 */

@SuppressWarnings("GroovyUnusedDeclaration")
class StringExtension {

    public static Yaml yaml = new Yaml()
    public static Gson gson = new Gson()

    /**
     * Translates a string into a colored string, use '&' as color character
     * @param self
     * @return colored string
     */
    public static String getColor(String self) {
        ChatColor.translateAlternateColorCodes '&' as char, self
    }

    /**
     * @see #getColor(String)
     */
    public static String getC(String self) {
        getColor self
    }

    /**
     * Strips this string of all color codes
     * @param self
     * @return copy of the input string, without any coloring
     */
    public static String getStripColor(String self) {
        ChatColor.stripColor self
    }

    /**
     * @see #getStripColor(String)
     */
    public static String getS(String self) {
        getStripColor self
    }

    /**
     * get player by name
     * @param self
     * @return player or null
     */
    public static Player getPlayer(String self) {
        Bukkit.getPlayer self
    }

    /**
     * @see #getPlayer(String)
     */
    public static Player getP(String self) {
        Bukkit.getPlayer self.trim()
    }

    /**
     * get offline player by name
     * @param self
     * @return offline player
     */
    public static OfflinePlayer getOfflinePlayer(String self) {
        Bukkit.getOfflinePlayer self.trim()
    }

    /**
     * @see #getOfflinePlayer(String)
     */
    public static OfflinePlayer getOfp(String self) {
        getOfflinePlayer self
    }

    /**
     * get world by name
     * @param self
     * @return world or null
     */
    public static World getWorld(String self) {
        Bukkit.getWorld self.trim()
    }

    /**
     * @see #getWorld(String)
     */
    public static World getW(String self) {
        getWorld self
    }

    /**
     * parse string as JSON
     * @param self
     * @return JSON object
     */
    public static Object getJson(String self) {
        gson.fromJson self, Object
    }

    /**
     * parse string as YAML
     * @param self
     * @return parse result
     */
    public static Object getYaml(String self) {
        yaml.load self
    }

    /**
     * convert string to character
     * @param self
     * @return first character
     */
    public static char getChar(String self) {
        self as char
    }

    /**
     * converts all of the characters in this String to upper case
     * @param self
     * @return
     */
    public static String getUp(String self) {
        self.toUpperCase()
    }

    /**
     * converts all of the characters in this String to lower case
     * @param self
     * @return
     */
    public static String getLow(String self) {
        self.toLowerCase()
    }


}
