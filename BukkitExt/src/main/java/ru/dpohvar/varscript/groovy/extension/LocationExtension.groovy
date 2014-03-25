package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
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

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class LocationExtension<L extends Location> {

    // as locations

    /**
     * clone location
     * @param self
     * @return location
     */
    public static Location getLoc(Location self) {
        self.clone()
    }

    /**
     * @see Location#getDirection()
     */
    public static Vector getDir(L self){
        self.direction
    }

    /**
     * change material of block in current location
     * @param self
     * @param material new material
     * @return self
     */
    public static L setBlock(L self, Material material) {
        self.block.type = material
        self
    }

    /**
     * change material of block in current location
     * @param self
     * @param material material id
     * @return self
     */
    public static L setBlock(L self, int material) {
        getLoc(self).block.typeId = material
        self
    }

    /**
     * change material of block in current location
     * @param self
     * @param material alias of material
     * @return self
     */
    public static L setBlock(L self, String material) {
        getLoc(self).block.type = BukkitExtUtils.parseEnum Material, material
        self
    }

    /**
     * change material and data of block in current location
     * @param self
     * @param material new material
     * @param data data
     * @return self
     */
    public static L setBlock(L self, Material material, byte data) {
        getLoc(self).block.type = material
        getLoc(self).block.data = data
        self
    }

    /**
     * change material and data of block in current location
     * @param self
     * @param material material id
     * @param data data
     * @return self
     */
    public static L setBlock(L self, int material, byte data) {
        getLoc(self).block.typeId = material
        getLoc(self).block.data = data
        self
    }

    /**
     * change material and data of block in current location
     * @param self
     * @param material alias of material
     * @param data data
     * @return self
     */
    public static L setBlock(L self, String material, byte data) {
        getLoc(self).block.type = BukkitExtUtils.parseEnum Material, material
        getLoc(self).block.data = data
        self
    }

    /**
     * teleport entity to current location
     * @param self
     * @param entity entity to be teleported
     * @return self
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
     * spawn entity in current location by type
     * @param self
     * @param type type of entity
     * @return spawned entity
     */
    public static Entity spawn(L self, EntityType type) {
        self.world.spawnEntity self, type
    }

    /**
     * spawn entity in current location by class
     * @param self
     * @param type class of entity
     * @return spawned entity
     */
    public static <V extends Entity> V spawn(L self, Class<V> type) {
        self.world.spawn((Location)self, (Class)type)
    }

    /**
     * spawn entity in current location by type of existing entity
     * @param self
     * @param type class of entity
     * @return spawned entity
     */
    public static <V extends Entity> V spawn(L self, V type) {
        self.world.spawnEntity self, type.type
    }

    /**
     * spawn entity in current location
     * @param self
     * @param type alias of entity type
     * @return spawned entity
     */
    public static Entity spawn(L self, String type) {
        self.world.spawnEntity self, BukkitExtUtils.parseEnum(EntityType, type)
    }

    /**
     * drop item in current location
     * @param self
     * @param type itemstack
     * @return entity item
     */
    public static Item spawn(L self, ItemStack type) {
        self.world.dropItem self, type
    }

    /**
     * spawn falling block in current location
     * @param self
     * @param material material of block
     * @param data additional data
     * @return falling block
     */
    public static FallingBlock spawn(L self, Material material, int data = 0) {
        self.world.spawnFallingBlock self, material, data as byte
    }

    /**
     * spawn falling block in current location
     * @param self
     * @param material material id
     * @param data additional data
     * @return falling block
     */
    public static FallingBlock spawn(L self, int material, int data = 0) {
        self.world.spawnFallingBlock self, material, data  as byte
    }

    /**
     * spawn falling block in current location
     * @param self
     * @param material material by alias
     * @param data additional data
     * @return falling block
     */
    public static FallingBlock spawn(L self, String material, int data) {
        self.world.spawnFallingBlock self, BukkitExtUtils.parseEnum(Material, material), data as byte
    }

    /**
     * create explosion in current location
     * @param self
     * @param power explosion power
     * @return self
     * @see org.bukkit.World#createExplosion(org.bukkit.Location, float)
     */
    public static L explode(L self, double power) {
        self.world.createExplosion self, power as float
        self
    }

    /**
     * @see #explode(L, double)
     */
    public static L ex(L self, double power) {
        explode self, power as float
    }

    /**
     * @see #explode(L, double)
     */
    public static L setEx(L self, double power) {
        explode self, power as float
    }

    /**
     * Strikes lightning at current location
     * @param self
     * @return LightningStrike
     */
    public static LightningStrike bolt(L self){
        self.world.strikeLightning self
    }

    /**
     * Strikes lightning at current location without doing damage
     * @param self
     * @return LightningStrike
     */
    public static LightningStrike fbolt(L self){
        self.world.strikeLightningEffect self
    }

    /**
     * get relative location up
     * @param self
     * @param y
     * @return location
     */
    public static Location up(L self, double y){
        def target = getLoc self
        target.y += y
        target
    }

    /**
     * get relative location down
     * @param self
     * @param y
     * @return location
     */
    public static Location down(L self, double y){
        def target = getLoc self
        target.y -= y
        target
    }

    /**
     * get relative location by x,y,z
     * @param self
     * @param x
     * @param y
     * @param z
     * @return location
     */
    public static Location move(L self, double x, double y, double z){
        def target = getLoc self
        target.add x, y, z
        target
    }

    /**
     * get relative location by y
     * @param self
     * @param y
     * @return location
     */
    public static Location move(L self, double y){
        move self, 0, y, 0
    }

    /**
     * get relative location by vector
     * @param self
     * @param vector
     * @return location
     */
    public static Location move(L self, Vector vector){
        def target = getLoc self
        target.add vector
        target
    }

    /**
     * get block from the current position
     * @param self
     * @param x shift by X
     * @param y shift by Y
     * @param z shift by Z
     * @return block
     * @see #rel(L, Vector)
     */
    public static Block rel(L self, int x, int y, int z){
        self.block.getRelative x, y, z
    }

    /**
     * get block from the current position
     * @param self
     * @param v shift by vector
     * @return block
     */
    public static Block rel(L self, Vector v){
        self.block.getRelative v.blockX, v.blockY, v.blockZ
    }

    /**
     * get block from the current position
     * @param self
     * @param y shift by Y
     * @return block
     * @see #rel(L, int, int, int)
     */
    public static Block rel(L self, int y){
        rel self, 0, y, 0
    }

    /**
     * get block relative the current position
     * @param self
     * @param face face of block
     * @param dist distance
     * @return block
     * @see Block#getRelative(org.bukkit.block.BlockFace, int)
     */
    public static Block rel(L self, BlockFace face, int dist=1){
        self.block.getRelative face, dist
    }

    /**
     * get block relative the current position
     * @param self
     * @param face alias of block face
     * @param dist distance
     * @return block
     * @see rel(org.bukkit.block.BlockFace, int)
     */
    public static Block rel(L self, String face, int dist=1){
        self.block.getRelative BukkitExtUtils.parseEnum(BlockFace, face), dist
    }

    /**
     * get distance between current location and another
     * @param self current location
     * @param tar another
     * @return distance
     */
    public static double dist(L self, Location tar){
        self.distance tar
    }

    /**
     * get distance between current location and another
     * @param self current location
     * @param tar another
     * @return distance
     */
    public static double dist(L self, Entity tar){
        self.distance tar.location
    }

    /**
     * get distance between current location and another
     * @param self current location
     * @param tar another
     * @return distance
     */
    public static double dist(L self, Block tar){
        self.distance tar.location
    }

    /**
     * get vector from current location to another
     * @param self current location
     * @param tar another location
     * @return vector or null
     */
    public static Vector minus(L self, Location tar){
        if (self.world != tar.world) return null
        new Vector(tar.x-self.x, tar.y-self.y, tar.z-self.z)
    }

    /**
     * get vector from current location to another
     * @param self current location
     * @param tar another location
     * @return vector or null
     */
    public static Vector minus(L self, Entity tar){
        minus self, tar.location
    }

    /**
     * get vector from current location to another
     * @param self current location
     * @param tar another location
     * @return vector or null
     */
    public static Vector minus(L self, Block tar){
        minus self, tar.location
    }

    // regions

    /**
     * create sphere in current location
     * @param self current location
     * @param radius radius of sphere
     * @return sphere
     */
    public static SphereRegion sphere(L self, double radius){
        new SphereRegion(self, radius)
    }

    /**
     * create flat round area in current location
     * @param self current location
     * @param radius radius of sphere
     * @return area
     */
    public static SphereArea sphereArea(L self, double radius){
        new SphereArea(self, radius)
    }

    /**
     * create new region centered in current location
     * @param self current location
     * @param radius distance from center to edge
     * @return region
     */
    public static CubeRegion cube(L self, double radius){
        new CubeRegion(self, radius, radius, radius)
    }

    /**
     * create new region centered in current location
     * @param self current location
     * @param x distance from center to edge by x
     * @param y distance from center to edge by y
     * @param z distance from center to edge by z
     * @return region
     */
    public static CubeRegion cube(L self, double x, double y, double z){
        new CubeRegion(self, x, y, z)
    }

    /**
     * create region between current location and another
     * @param self current location
     * @param loc another location
     * @return region
     */
    public static CubeRegion cube(L self, Location loc){
        new CubeRegion(self, loc)
    }

    /**
     * create region between current location and another
     * @param self current location
     * @param loc another location
     * @return region
     */
    public static CubeRegion cube(L self, Entity loc){
        new CubeRegion(self, loc.location)
    }

    /**
     * create region between current location and another
     * @param self current location
     * @param loc another location
     * @return region
     */
    public static CubeRegion cube(L self, Block loc){
        new CubeRegion(self, loc.location)
    }

    /**
     * create flat area centered in current location
     * @param self current location
     * @param radius distance from center to edge
     * @return region
     */
    public static CubeArea cubeArea(L self, double radius){
        new CubeArea(self, radius, radius)
    }

    /**
     * create new flat area centered in current location
     * @param self current location
     * @param x distance from center to edge by x
     * @param y distance from center to edge by y
     * @param z distance from center to edge by z
     * @return region
     */
    public static CubeArea cubeArea(L self, double x, double z){
        new CubeArea(self, x, z)
    }

    /**
     * create flat area between current location and another
     * @param self current location
     * @param loc another location
     * @return region
     */
    public static CubeArea cubeArea(L self, Location loc){
        new CubeArea(self, loc)
    }

    /**
     * create flat area between current location and another
     * @param self current location
     * @param loc another location
     * @return region
     */
    public static CubeArea cubeArea(L self, Entity loc){
        new CubeArea(self, loc.location)
    }

    /**
     * create flat area between current location and another
     * @param self current location
     * @param loc another location
     * @return region
     */
    public static CubeArea cubeArea(L self, Block loc){
        new CubeArea(self, loc.location)
    }

    /**
     * get all nearby entities in radius
     * @param self current location
     * @param radius
     * @return list of entities (without self)
     */
    public static List<Entity> nearby(L self, double radius){
        self.world.entities.findAll { Entity it ->
            it.location.distance(self) <= radius
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
        self.world.playSound self, snd, vol as float, pitch as float
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
