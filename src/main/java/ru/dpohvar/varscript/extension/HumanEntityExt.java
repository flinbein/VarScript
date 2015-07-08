package ru.dpohvar.varscript.extension;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class HumanEntityExt {

    public static GameMode getGm(HumanEntity self) {
        return self.getGameMode();
    }

    public static void setGm(HumanEntity self, GameMode mode) {
        self.setGameMode(mode);
    }

    public static void setGm(HumanEntity self, int mode) {
        setGm(self, GameMode.getByValue(mode));
    }

    public static ItemStack getCursor(HumanEntity self) {
        return self.getItemOnCursor();
    }

    public static void setCursor(HumanEntity self, ItemStack cursor) {
        self.setItemOnCursor(cursor);
    }
}
