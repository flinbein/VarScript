package ru.dpohvar.varscript.extension

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.CommandBlock
import org.bukkit.block.Sign
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ru.dpohvar.varscript.groovy.BukkitExtUtils
import ru.dpohvar.varscript.region.CubeArea
import ru.dpohvar.varscript.region.CubeRegion
import ru.dpohvar.varscript.region.SphereArea
import ru.dpohvar.varscript.region.SphereRegion

/**
 * Created by DPOH-VAR on 06.03.14
 */

class BlockExtension<L extends Block> {

    public static String getName(Block self){
        def state = self.state
        if (state instanceof CommandBlock) self.name
        else null
    }

    public static L setName(L self, String name){
        def state = self.state
        if (state instanceof CommandBlock) {
            state.name = name
            state.update()
        }
        self
    }

    public static String getCommand(Block self){
        def state = self.state
        if (state instanceof CommandBlock) self.command
        else null
    }

    public static L setCommand(L self, String command){
        def state = self.state
        if (state instanceof CommandBlock) {
            state.command = command
            state.update()
        }
        self
    }

    public static List getLines(Block self){
        def state = self.state
        if (state instanceof Sign) {
            Sign sign = state as Sign
            def list = sign.lines as ObservableList
            list.addPropertyChangeListener({ event ->
                4.times{
                    sign.setLine it, list[it] as String
                }
                sign.update()
            })
            list
        }
        else null
    }

    public static L setLines(L self, List lines){
        def state = self.state
        if (state instanceof Sign) {
            Sign sign = state as Sign
            def list = sign.lines as ObservableList
            list.addPropertyChangeListener({ event ->
                4.times{
                    sign.setLine it, list[it] as String
                }
                sign.update()
            })
            list
        }
        self
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////// as locations //////////////////////////////////////////////////

    /**
     * get location of block
     * @param self
     * @return
     */
    public static Location getLoc(Block self) {
        self.location
    }

    /**
     * @see LocationExtension#getDir(LocationExtension.L)
     */
    public static Vector getDir(Block self) {
        getLoc self direction
    }

    /**
     * useless method
     * @param self
     * @return self
     */
    public static <L extends Block> L getBlock(L self) {
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L, Material)
     */
    public static L setBlock(Block self, Material material) {
        self.type = material
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L, int)
     */
    public static L setBlock(Block self, int material) {
        self.typeId = material
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L, String)
     */
    public static L setBlock(Block self, String material) {
        self.type = BukkitExtUtils.parseEnum Material, material
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L, Material, byte)
     */
    public static L setBlock(Block self, Material material, byte data) {
        self.type = material
        self.data = data
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L, int, byte)
     */
    public static L setBlock(Block self, int material, byte data) {
        self.typeId = material
        self.data = data
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L, String, byte)
     */
    public static L setBlock(Block self, String material, byte data) {
        self.type = BukkitExtUtils.parseEnum Material, material
        self.data = data
        self
    }

    /**
     * @see LocationExtension#tpHere(LocationExtension.L, Entity)
     */
    public static L tpHere(Block self, Entity entity) {
        Location target = getLoc self
        target.pitch = entity.location.pitch
        target.yaw = entity.location.yaw
        entity.teleport(target)
        self
    }

    /**
     * @see #tpHere(L, Entity)
     */
    public static L leftShift(Block self, Entity entity) {
        tpHere self, entity
    }

    /**
     * @see Location#getX()
     */
    public static double getX(Block self) {
        getLoc self x
    }

    /**
     * @see Location#getY()
     */
    public static double getY(Block self) {
        getLoc self y
    }

    /**
     * @see Location#getZ()
     */
    public static double getZ(Block self) {
        getLoc self z
    }

    /**
     * @see Location#getPitch()
     */
    public static float getPitch(Block self) {
        getLoc self pitch
    }

    /**
     * @see Location#getYaw()
     */
    public static float getYaw(Block self) {
        getLoc self yaw
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, EntityType)
     */
    public static Entity spawn(Block self, EntityType type) {
        def target = getLoc self
        target.world.spawnEntity target, type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, Class)
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    public static <V extends Entity> V spawn(Block self, Class<V> type) {
        self.world.spawn self.location, type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, Entity)
     */
    public static <V extends Entity> V spawn(Block self, V type) {
        def target = getLoc self
        target.world.spawnEntity target, type.type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, String)
     */
    public static Entity spawn(Block self, String type) {
        def target = getLoc self
        target.world.spawnEntity target, BukkitExtUtils.parseEnum(EntityType, type)
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, ItemStack)
     */
    public static Item spawn(Block self, ItemStack type) {
        def target = getLoc self
        target.world.dropItem target, type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, Material, byte)
     */
    public static FallingBlock spawn(Block self, Material material, byte data = 0) {
        def target = getLoc self
        target.world.spawnFallingBlock target, material, data
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, int, byte)
     */
    public static FallingBlock spawn(Block self, int material, byte data = 0) {
        def target = getLoc self
        target.world.spawnFallingBlock target, material, data
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L, String, byte)
     */
    public static FallingBlock spawn(Block self, String material, byte data) {
        def target = getLoc self
        target.world.spawnFallingBlock target, BukkitExtUtils.parseEnum(Material, material), data
    }

    /**
     * @see LocationExtension#explode(LocationExtension.L, float)
     */
    public static L explode(Block self, float power) {
        def target = getLoc self
        target.world.createExplosion target, power
        self
    }

    /**
     * @see LocationExtension#explode(LocationExtension.L, float)
     */
    public static L ex(Block self, float power) {
        explode self, power
    }

    /**
     * @see LocationExtension#explode(LocationExtension.L, float)
     */
    public static L setEx(Block self, float power) {
        explode self, power
    }

    /**
     * @see LocationExtension#bolt(LocationExtension.L)
     */
    public static LightningStrike bolt(Block self) {
        def target = getLoc self
        target.world.strikeLightning target
    }

    /**
     * @see LocationExtension#fbolt(LocationExtension.L)
     */
    public static LightningStrike fbolt(Block self) {
        def target = getLoc self
        target.world.strikeLightningEffect target
    }

    /**
     * @see LocationExtension#up(LocationExtension.L, double)
     */
    public static Location up(Block self, double y) {
        def target = getLoc self
        target.y += y
        target
    }

    /**
     * @see LocationExtension#down(LocationExtension.L, double)
     */
    public static Location down(Block self, double y) {
        def target = getLoc self
        target.y -= y
        target
    }

    /**
     * @see LocationExtension#move(LocationExtension.L, double, double, double)
     */
    public static Location move(Block self, double x, double y, double z) {
        def target = getLoc self
        target.add x, y, z
        target
    }

    /**
     * @see LocationExtension#move(LocationExtension.L, double)
     */
    public static Location move(Block self, double y) {
        move self, 0, y, 0
    }

    /**
     * @see LocationExtension#move(LocationExtension.L, Vector)
     */
    public static Location move(Block self, Vector vector) {
        def target = getLoc self
        target.add vector
        target
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L, int, int, int)
     */
    public static Block rel(Block self, int x, int y, int z) {
        self.getRelative x, y, z
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L, Vector)
     */
    public static Block rel(Block self, Vector v) {
        self.getRelative v.blockX, v.blockY, v.blockZ
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L, int)
     */
    public static Block rel(Block self, int y) {
        rel self, 0, y, 0
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L, BlockFace, int)
     */
    public static Block rel(Block self, BlockFace face, int dist) {
        self.getRelative face, dist
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L, String, int)
     */
    public static Block rel(Block self, String face, int dist) {
        self.getRelative BukkitExtUtils.parseEnum(BlockFace, face), dist
    }

    /**
     * @see LocationExtension#dist(LocationExtension.L, Location)
     */
    public static double dist(Block self, Location tar) {
        getLoc self distance tar
    }

    /**
     * @see LocationExtension#dist(LocationExtension.L, Entity)
     */
    public static double dist(Block self, Entity tar) {
        getLoc self distance tar.location
    }

    /**
     * @see LocationExtension#dist(LocationExtension.L, Block)
     */
    public static double dist(Block self, Block tar) {
        getLoc self distance tar.location
    }

    /**
     * @see LocationExtension#minus(LocationExtension.L, Location)
     */
    public static Vector minus(Block self, Location locA) {
        def locB = getLoc self
        new Vector(locB.x - locA.x, locB.y - locA.y, locB.z - locA.z)
    }

    /**
     * @see LocationExtension#minus(LocationExtension.L, Entity)
     */
    public static Vector minus(Block self, Entity ent) {
        minus self, ent.location
    }

    /**
     * @see LocationExtension#minus(LocationExtension.L, Block)
     */
    public static Vector minus(Block self, Block ent) {
        minus self, ent.location
    }

    // regions

    /**
     * @see LocationExtension#sphere(LocationExtension.L, double)
     */
    public static SphereRegion sphere(Block self, double radius) {
        new SphereRegion(getLoc(self), radius)
    }

    /**
     * @see LocationExtension#sphereArea(LocationExtension.L, double)
     */
    public static SphereArea sphereArea(Block self, double radius) {
        new SphereArea(getLoc(self), radius)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L, double)
     */
    public static CubeRegion cube(Block self, double radius) {
        new CubeRegion(getLoc(self), radius, radius, radius)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L, double, double, double)
     */
    public static CubeRegion cube(Block self, double x, double y, double z) {
        new CubeRegion(getLoc(self), x, y, z)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L, Location)
     */
    public static CubeRegion cube(Block self, Location other) {
        new CubeRegion(getLoc(self), other)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L, Entity)
     */
    public static CubeRegion cube(Block self, Entity other) {
        new CubeRegion(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L, Block)
     */
    public static CubeRegion cube(Block self, Block other) {
        new CubeRegion(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L, double)
     */
    public static CubeArea cubeArea(Block self, double radius) {
        new CubeArea(getLoc(self), radius, radius)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L, double, double)
     */
    public static CubeArea cubeArea(Block self, double x, double z) {
        new CubeArea(getLoc(self), x, z)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L, Location)
     */
    public static CubeArea cubeArea(Block self, Location other) {
        new CubeArea(getLoc(self), other)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L, Entity)
     */
    public static CubeArea cubeArea(Block self, Entity other) {
        new CubeArea(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L, Block)
     */
    public static CubeArea cubeArea(Block self, Block other) {
        new CubeArea(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#nearby(LocationExtension.L, double)
     */
    public static List<Entity> nearby(Block self, double radius) {
        Location loc = getLoc self
        self.world.entities.findAll { Entity it ->
            it.location.distance(loc) <= radius
        } as List
    }

    /**
     * play sound at current location
     * @param self
     * @param snd sound to play
     * @param vol volume of the sound
     * @param pitch pitch of the sound
     * @return self
     */
    public static L sound(Block self, Sound snd, double vol = 1, double pitch = 1) {
        self.world.playSound self.location, snd, vol as float, pitch as float
        self
    }

    /**
     * play sound at current location
     * @param self
     * @param snd alias of sound name
     * @param vol volume of the sound
     * @param pitch pitch of the sound
     * @return self
     */
    public static L sound(Block self, String snd, double vol = 1, double pitch = 1) {
        sound self, BukkitExtUtils.parseEnum(Sound, snd), vol, pitch
    }

}
