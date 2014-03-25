package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import ru.dpohvar.varscript.groovy.nbt.NBTCompound
import ru.dpohvar.varscript.groovy.nbt.NBTUtils

/**
 * Created by DPOH-VAR on 06.03.14.
 */
@CompileStatic
class NBTExtension {

    @CompileStatic(TypeCheckingMode.SKIP)
    public static NBTCompound getNbt(Entity self) {
        def basic = NBTUtils.classNBTTagCompound.newInstance()
        self.handle.e basic
        NBTCompound.forNBT basic
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public static boolean setNbt(Entity self, NBTCompound val) {
        self.handle.f val.@handle
        true
    }

    public static boolean setNbt(Entity self, Map val) {
        setNbt self, new NBTCompound(val)
    }

    public static <T extends Entity> T addNbt(T self, Map val) {
        def nbt = getNbt self
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setNbt self, nbt
        self
    }

    public static Object nbt(Entity self, Closure closure) {
        def val = getNbt self
        def ext = (val) ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt self, ext
        result
    }

    public static<T extends Entity> T nbt(T self, Map val) {
        setNbt self, val
        self
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public static NBTCompound getNbt(Block self) {
        Object tile = self.world.getTileEntityAt(self.x, self.y, self.z)
        if (!tile) return null
        def basic = NBTUtils.conNBTTagCompound.create()
        tile.b basic
        NBTCompound result = NBTCompound.forNBT(basic)
        return result
    }

    private static int maxDist = Bukkit.server.viewDistance * 32
    @CompileStatic(TypeCheckingMode.SKIP)
    public static boolean setNbt(Block self, NBTCompound val) {
        Object tile = self.world.getTileEntityAt self.x, self.y, self.z
        if (!tile) return false
        val = val.clone()
        val.x = self.x
        val.y = self.y
        val.z = self.z
        tile.a(val.getHandle())
        def packet = tile.updatePacket
        self.world.players.findAll {
            it.location.distance(self.location) < maxDist
        } each {
            it.handle.playerConnection.sendPacket packet
        }
        return true
    }

    public static boolean setNbt(Block self, Map val) {
        setNbt self, new NBTCompound(val)
    }

    public static <T extends Block> T addNbt(T self, Map val) {
        def nbt = getNbt self
        if (nbt == null) return self
        nbt.merge val
        setNbt self, nbt
        self
    }

    public static Object nbt(Block self, Closure closure) {
        def val = getNbt self
        def ext = (val) ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt self, ext
        result
    }

    public static<T extends Block> T nbt(T self, Map val) {
        setNbt self, val
        self
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public static NBTCompound getNbt(ItemStack self) {
        //TODO: сделать проверку, что это CIS, тогда копируем мету
        def tag = self.handle.tag
        if (tag == null) null
        else NBTCompound.forNBT(tag.clone())
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public static boolean setNbt(ItemStack item, NBTCompound val) {
        try {
            if (NBTUtils.classCraftItemStack.isInstance(item)) {
                item.handle.tag = val.getHandle().clone()
            } else {
                ItemStack copyItem = NBTUtils.classCraftItemStack.asCraftCopy(item)
                copyItem.handle.tag = val.getHandle().clone()
                item.itemMeta = copyItem.itemMeta;
            }
            return true
        } catch (ignored) {
            return false
        }
    }

    public static boolean setNbt(ItemStack item, Map val) {
        setNbt item, new NBTCompound(val)
    }

    public static <T extends ItemStack> T addNbt(T self, Map val) {
        def nbt = getNbt self
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setNbt self, nbt
        self
    }

    public static Object nbt(ItemStack self, Closure closure) {
        def val = getNbt self
        def ext = (val) ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt self, ext
        result
    }

    public static<T extends ItemStack> T nbt(T self, Map val) {
        setNbt self, val
        self
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public static NBTCompound getNbt(File file) {
        try {
            file.withInputStream { input ->
                def tag = NBTUtils.rcNBTCompressedStreamTools.realClass.a(input)
                if (tag == null) return null
                else return NBTCompound.forNBT(tag)
            } as NBTCompound
        } catch (ignored) {
            null
        }
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    public static boolean setNbt(File self, NBTCompound val) {
        if (self == null || val == null) return false
        if (!self.isFile()) {
            File parent = self.parentFile
            if (parent == null) parent = new File('.')
            parent.mkdirs()
            if (!self.createNewFile()) return false
        }
        try {
            self.withOutputStream { output ->
                NBTUtils.rcNBTCompressedStreamTools.realClass.a(val.@handle, output)
                return true
            }
        } catch (ignored) {
            return false
        }
    }

    public static boolean setNbt(File self, Map val) {
        setNbt self, new NBTCompound(val)
    }

    public static <T extends File> T addNbt(T self, Map val) {
        def nbt = getNbt self
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setNbt self, nbt
        self
    }

    public static Object nbt(File self, Closure closure) {
        def val = getNbt self
        def ext = (val) ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt self, ext
        result
    }

    public static<T extends File> T nbt(T self, Map val) {
        setNbt self, val
        self
    }

    private static File playersFolder = new File(Bukkit.worlds.get(0).worldFolder, 'players')

    public static NBTCompound getNbt(String self) {
        getNbt new File(playersFolder, "${self}.dat")
    }

    public static boolean setNbt(String self, NBTCompound val) {
        setNbt new File(playersFolder, "${self}.dat"), val
    }

    public static boolean setNbt(String self, Map val) {
        setNbt self, new NBTCompound(val)
    }


    public static <T extends String> T addNbt(T self, Map val) {
        def nbt = getNbt self
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setNbt self, nbt
        self
    }

    public static Object nbt(String self, Closure closure) {
        def val = getNbt self
        def ext = (val) ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setNbt self, ext
        result
    }

    public static<T extends String> T nbt(T self, Map val) {
        setNbt self, val
        self
    }

    public static NBTCompound getOfflineNbt(OfflinePlayer self) {
        getNbt new File(playersFolder, "${self.name}.dat")
    }

    public static boolean setOfflineNbt(OfflinePlayer self, NBTCompound val) {
        setNbt new File(playersFolder, "${self.name}.dat"), val
    }

    public static boolean setOfflineNbt(OfflinePlayer self, Map val) {
        setOfflineNbt self, new NBTCompound(val)
    }


    public static <T extends OfflinePlayer> T addOfflineNbt(T self, Map val) {
        def nbt = getOfflineNbt self
        if (nbt == null) nbt = new NBTCompound()
        nbt.merge val
        setOfflineNbt self, nbt
        self
    }

    public static Object offlineNbt(OfflinePlayer self, Closure closure) {
        def val = getOfflineNbt self
        def ext = (val) ? val.clone() : new NBTCompound()
        def result = closure ext
        if (ext != val) setOfflineNbt self, ext
        result
    }

    public static<T extends OfflinePlayer> T offlineNbt(T self, Map val) {
        setOfflineNbt self, val
        self
    }


}
