package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
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

@SuppressWarnings(["GroovyUnusedDeclaration", "GrDeprecatedAPIUsage"])
@CompileStatic
class BlockExtension<L extends Block> {

    // as locations

    /**
     * get location of block
     * @param self
     * @return
     */
    public static Location getLoc(L self) {
        self.location
    }

    /**
     * @see LocationExtension#getDir(LocationExtension.L)
     */
    public static Vector getDir(L self){
        getLoc self direction
    }

    /**
     * useless method
     * @param self
     * @return self
     */
    public static L getBlock(L self) {
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L,Material)
     */
    public static L setBlock(L self, Material material) {
        self.type = material
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L,int)
     */
    public static L setBlock(L self, int material) {
        self.typeId = material
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L,String)
     */
    public static L setBlock(L self, String material) {
        self.type = BukkitExtUtils.parseEnum Material, material
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L,Material,byte)
     */
    public static L setBlock(L self, Material material, byte data) {
        self.type = material
        self.data = data
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L,int,byte)
     */
    public static L setBlock(L self, int material, byte data) {
        self.typeId = material
        self.data = data
        self
    }

    /**
     * @see LocationExtension#setBlock(LocationExtension.L,String,byte)
     */
    public static L setBlock(L self, String material, byte data) {
        self.type = BukkitExtUtils.parseEnum Material, material
        self.data = data
        self
    }

    /**
     * @see LocationExtension#tpHere(LocationExtension.L,Entity)
     */
    public static L tpHere(L self, Entity entity) {
        Location target = getLoc self
        target.pitch = entity.location.pitch
        target.yaw = entity.location.yaw
        entity.teleport(target)
        self
    }

    /**
     * @see #tpHere(L,Entity)
     */
    public static L leftShift(L self, Entity entity){
        tpHere self, entity
    }

    /**
     * @see Location#getX()
     */
    public static double getX(L self) {
        getLoc self x
    }

    /**
     * @see Location#getY()
     */
    public static double getY(L self) {
        getLoc self y
    }

    /**
     * @see Location#getZ()
     */
    public static double getZ(L self) {
        getLoc self z
    }

    /**
     * @see Location#getPitch()
     */
    public static float getPitch(L self) {
        getLoc self pitch
    }

    /**
     * @see Location#getYaw()
     */
    public static float getYaw(L self) {
        getLoc self yaw
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,EntityType)
     */
    public static Entity spawn(L self, EntityType type) {
        def target = getLoc self
        target.world.spawnEntity target, type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,Class)
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    public static <V extends Entity> V spawn(L self, Class<V> type) {
        self.world.spawn self.location, type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,Entity)
     */
    public static <V extends Entity> V spawn(L self, V type) {
        def target = getLoc self
        target.world.spawnEntity target, type.type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,String)
     */
    public static Entity spawn(L self, String type) {
        def target = getLoc self
        target.world.spawnEntity target, BukkitExtUtils.parseEnum(EntityType, type)
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,ItemStack)
     */
    public static Item spawn(L self, ItemStack type) {
        def target = getLoc self
        target.world.dropItem target, type
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,Material,byte)
     */
    public static FallingBlock spawn(L self, Material material, byte data = 0) {
        def target = getLoc self
        target.world.spawnFallingBlock target, material, data
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,int,byte)
     */
    public static FallingBlock spawn(L self, int material, byte data = 0) {
        def target = getLoc self
        target.world.spawnFallingBlock target, material, data
    }

    /**
     * @see LocationExtension#spawn(LocationExtension.L,String,byte)
     */
    public static FallingBlock spawn(L self, String material, byte data) {
        def target = getLoc self
        target.world.spawnFallingBlock target, BukkitExtUtils.parseEnum(Material, material), data
    }

    /**
     * @see LocationExtension#explode(LocationExtension.L,float)
     */
    public static L explode(L self, float power) {
        def target = getLoc self
        target.world.createExplosion target, power
        self
    }

    /**
     * @see LocationExtension#explode(LocationExtension.L,float)
     */
    public static L ex(L self, float power) {
        explode self, power
    }

    /**
     * @see LocationExtension#explode(LocationExtension.L,float)
     */
    public static L setEx(L self, float power) {
        explode self, power
    }

    /**
     * @see LocationExtension#bolt(LocationExtension.L)
     */
    public static LightningStrike bolt(L self){
        def target = getLoc self
        target.world.strikeLightning target
    }

    /**
     * @see LocationExtension#fbolt(LocationExtension.L)
     */
    public static LightningStrike fbolt(L self){
        def target = getLoc self
        target.world.strikeLightningEffect target
    }

    /**
     * @see LocationExtension#up(LocationExtension.L,double)
     */
    public static Location up(L self, double y){
        def target = getLoc self
        target.y += y
        target
    }

    /**
     * @see LocationExtension#down(LocationExtension.L,double)
     */
    public static Location down(L self, double y){
        def target = getLoc self
        target.y -= y
        target
    }

    /**
     * @see LocationExtension#move(LocationExtension.L,double,double,double)
     */
    public static Location move(L self, double x, double y, double z){
        def target = getLoc self
        target.add x, y, z
        target
    }

    /**
     * @see LocationExtension#move(LocationExtension.L,double)
     */
    public static Location move(L self, double y){
        move self, 0, y, 0
    }

    /**
     * @see LocationExtension#move(LocationExtension.L,Vector)
     */
    public static Location move(L self, Vector vector){
        def target = getLoc self
        target.add vector
        target
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L,int,int,int)
     */
    public static Block rel(L self, int x, int y, int z){
        self.getRelative x, y, z
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L,Vector)
     */
    public static Block rel(L self, Vector v){
        self.getRelative v.blockX, v.blockY, v.blockZ
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L,int)
     */
    public static Block rel(L self, int y){
        rel self, 0, y, 0
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L,BlockFace,int)
     */
    public static Block rel(L self, BlockFace face, int dist){
        self.getRelative face, dist
    }

    /**
     * @see LocationExtension#rel(LocationExtension.L,String,int)
     */
    public static Block rel(L self, String face, int dist){
        self.getRelative BukkitExtUtils.parseEnum(BlockFace, face), dist
    }

    /**
     * @see LocationExtension#dist(LocationExtension.L,Location)
     */
    public static double dist(L self, Location tar){
        getLoc self distance tar
    }

    /**
     * @see LocationExtension#dist(LocationExtension.L,Entity)
     */
    public static double dist(L self, Entity tar){
        getLoc self distance tar.location
    }

    /**
     * @see LocationExtension#dist(LocationExtension.L,Block)
     */
    public static double dist(L self, Block tar){
        getLoc self distance tar.location
    }

    /**
     * @see LocationExtension#minus(LocationExtension.L,Location)
     */
    public static Vector minus(L self, Location locA){
        def locB = getLoc self
        new Vector(locB.x-locA.x, locB.y-locA.y, locB.z-locA.z)
    }

    /**
     * @see LocationExtension#minus(LocationExtension.L,Entity)
     */
    public static Vector minus(L self, Entity ent){
        minus self, ent.location
    }

    /**
     * @see LocationExtension#minus(LocationExtension.L,Block)
     */
    public static Vector minus(L self, Block ent){
        minus self, ent.location
    }

    // regions

    /**
     * @see LocationExtension#sphere(LocationExtension.L,double)
     */
    public static SphereRegion sphere(L self, double radius){
        new SphereRegion(getLoc(self), radius)
    }

    /**
     * @see LocationExtension#sphereArea(LocationExtension.L,double)
     */
    public static SphereArea sphereArea(L self, double radius){
        new SphereArea(getLoc(self), radius)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L,double)
     */
    public static CubeRegion cube(L self, double radius){
        new CubeRegion(getLoc(self), radius, radius, radius)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L,double,double,double)
     */
    public static CubeRegion cube(L self, double x, double y, double z){
        new CubeRegion(getLoc(self), x, y, z)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L,Location)
     */
    public static CubeRegion cube(L self, Location other){
        new CubeRegion(getLoc(self), other)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L,Entity)
     */
    public static CubeRegion cube(L self, Entity other){
        new CubeRegion(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#cube(LocationExtension.L,Block)
     */
    public static CubeRegion cube(L self, Block other){
        new CubeRegion(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L,double)
     */
    public static CubeArea cubeArea(L self, double radius){
        new CubeArea(getLoc(self), radius, radius)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L,double,double)
     */
    public static CubeArea cubeArea(L self, double x, double z){
        new CubeArea(getLoc(self), x, z)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L,Location)
     */
    public static CubeArea cubeArea(L self, Location other){
        new CubeArea(getLoc(self), other)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L,Entity)
     */
    public static CubeArea cubeArea(L self, Entity other){
        new CubeArea(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#cubeArea(LocationExtension.L,Block)
     */
    public static CubeArea cubeArea(L self, Block other){
        new CubeArea(getLoc(self), other.location)
    }

    /**
     * @see LocationExtension#nearby(LocationExtension.L,double)
     */
    public static List<Entity> nearby(L self, double radius){
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
    public static L sound(L self, Sound snd, double vol=1, double pitch=1) {
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
    public static L sound(L self, String snd, double vol=1, double pitch=1) {
        sound self, BukkitExtUtils.parseEnum(Sound,snd), vol, pitch
    }

}
