package ru.dpohvar.varscript.extension

import org.bukkit.GameMode
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.ItemStack
import ru.dpohvar.varscript.groovy.BukkitExtUtils

/**
 * Created by DPOH-VAR on 06.03.14
 */

@SuppressWarnings(["GroovyUnusedDeclaration", "GrDeprecatedAPIUsage"])
class HumanEntityExtension<Z extends HumanEntity> {

    /**
     * get game mode
     * @param self
     * @return current game mode
     */
    public static GameMode getGm(Z self) {
        self.gameMode
    }

    /**
     * set game mode of human
     * @param self
     * @param mode game mode
     * @return self
     */
    public static Z setGm(Z self, GameMode mode) {
        self.gameMode = mode
        self
    }

    /**
     * set game mode of human
     * @param self
     * @param mode game mode by id
     * @return self
     */
    public static Z setGm(Z self, int mode) {
        setGm self, GameMode.getByValue(mode)
    }

    /**
     * set game mode of human
     * @param self
     * @param mode game mode by alias
     * @return self
     */
    public static Z setGm(Z self, String mode) {
        setGm self, BukkitExtUtils.parseEnum(GameMode, mode)
    }

    /**
     * returns the ItemStack currently on your cursor
     * @param self
     * @return the ItemStack of the item you are currently moving around
     */
    public static ItemStack getCursor(Z self) {
        self.itemOnCursor
    }

    /**
     * Sets the item to the given ItemStack, this will replace whatever the user was moving
     * @param self
     * @param cursor the ItemStack which will end up in the hand
     * @return self
     */
    public static Z setCursor(Z self, ItemStack cursor) {
        self.itemOnCursor = cursor
        self
    }

}
