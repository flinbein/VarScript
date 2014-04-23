package ru.dpohvar.varscript.extension

import groovy.transform.CompileStatic
import me.dpohvar.powernbt.api.NBTCompound
import me.dpohvar.powernbt.api.NBTManager
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

@CompileStatic
class PowerNBTExtension {

    static NBTManager nbtManager = NBTManager.nbtManager;

    public static NBTCompound getNbt(Entity entity) {
        nbtManager.read entity
    }

    public static boolean setNbt(Entity entity, NBTCompound val) {
        nbtManager.write entity, val
        true
    }

    public static boolean setNbt(Entity entity, Map val) {
        setNbt entity, new NBTCompound(val)
    }

    public static <T extends Entity> T addNbt(T entity, Map val) {
        def nbt = getNbt entity
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setNbt entity, nbt
        entity
    }

    public static Object nbt(Entity entity, Closure closure) {
        def val = getNbt entity
        def ext = val ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt entity, ext
        result
    }

    public static <T extends Entity> T nbt(T entity, Map val) {
        setNbt entity, val
        entity
    }

    public static NBTCompound getForgeData(Entity entity) {
        nbtManager.readForgeData entity
    }

    public static boolean setForgeData(Entity entity, NBTCompound val) {
        nbtManager.writeForgeData entity, val
        true
    }

    public static boolean setForgeData(Entity entity, Map val) {
        setForgeData entity, new NBTCompound(val)
    }

    public static <T extends Entity> T addForgeData(T entity, Map val) {
        def nbt = getForgeData entity
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setForgeData entity, nbt
        entity
    }

    public static Object forgeData(Entity entity, Closure closure) {
        def val = getForgeData entity
        def ext = val ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setForgeData entity, ext
        result
    }

    public static <T extends Entity> T forgeData(T entity, Map val) {
        setForgeData entity, val
        entity
    }

    public static NBTCompound getNbt(Block block) {
        nbtManager.read block
    }

    private static int maxDist = Bukkit.server.viewDistance * 32

    public static boolean setNbt(Block block, NBTCompound val) {
        val = val.clone()
        val.x = block.x
        val.y = block.y
        val.z = block.z
        nbtManager.write block, val
        true
    }

    public static boolean setNbt(Block block, Map val) {
        setNbt block, new NBTCompound(val)
    }

    public static <T extends Block> T addNbt(T block, Map val) {
        def nbt = getNbt block
        if (nbt == null) return block
        nbt.merge val
        setNbt block, nbt
        block
    }

    public static Object nbt(Block block, Closure closure) {
        def val = getNbt block
        def ext = val ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt block, ext
        result
    }

    public static <T extends Block> T nbt(T block, Map val) {
        setNbt block, val
        block
    }

    public static NBTCompound getNbt(ItemStack item) {
        nbtManager.read item
    }

    public static boolean setNbt(ItemStack item, NBTCompound val) {
        nbtManager.write item, val
        true
    }

    public static boolean setNbt(ItemStack item, Map val) {
        setNbt item, new NBTCompound(val)
    }

    public static <T extends ItemStack> T addNbt(T item, Map val) {
        def nbt = getNbt item
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setNbt item, nbt
        item
    }

    public static Object nbt(ItemStack item, Closure closure) {
        def val = getNbt item
        def ext = val ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt item, ext
        result
    }

    public static <T extends ItemStack> T nbt(T item, Map val) {
        setNbt item, val
        item
    }

    public static Object getNbt(File file) {
        nbtManager.read file
    }

    public static boolean setNbt(File file, Object val) {
        try {
            nbtManager.write file, val
            true
        } catch (ignored) {
            false
        }
    }

    public static Object nbt(File file, Closure closure) {
        def val = getNbt file
        def result = closure val
        setNbt file, val
        result
    }

    public static <T extends File> T nbt(T file, def val) {
        setNbt file, val
        file
    }

    public static NBTCompound getOfflineNbt(OfflinePlayer player) {
        nbtManager.readOfflinePlayer player
    }

    public static boolean setOfflineNbt(OfflinePlayer player, NBTCompound val) {
        nbtManager.writeOfflinePlayer player, val
    }

    public static boolean setOfflineNbt(OfflinePlayer player, Map val) {
        setOfflineNbt player, new NBTCompound(val)
    }


    public static <T extends OfflinePlayer> T addOfflineNbt(T player, Map val) {
        def nbt = getOfflineNbt player
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setOfflineNbt player, nbt
        player
    }

    public static Object offlineNbt(OfflinePlayer player, Closure closure) {
        def val = getOfflineNbt player
        def ext = val ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setOfflineNbt player, ext
        result
    }

    public static <T extends OfflinePlayer> T offlineNbt(T player, Map val) {
        setOfflineNbt player, val
        player
    }

}
