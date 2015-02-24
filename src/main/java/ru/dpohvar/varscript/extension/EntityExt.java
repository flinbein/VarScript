package ru.dpohvar.varscript.extension;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.dpohvar.varscript.extension.region.BoxArea;
import ru.dpohvar.varscript.extension.region.BoxRegion;
import ru.dpohvar.varscript.extension.region.SphereArea;
import ru.dpohvar.varscript.extension.region.SphereRegion;

import java.util.Iterator;
import java.util.List;

public class EntityExt {

    // getters

    public static Location getLoc(Entity self){
        return self.getLocation();
    }

    public static Vector getDir(Entity self){
        return getLoc(self).getDirection();
    }

    public static Block getBl(Entity self){
        return getLoc(self).getBlock();
    }

    public static double getX(Entity self){
        return getLoc(self).getX();
    }

    public static double getY(Entity self){
        return getLoc(self).getY();
    }

    public static float getPitch(Entity self){
        return getLoc(self).getPitch();
    }

    public static float getYaw(Entity self){
        return getLoc(self).getYaw();
    }

    public static Chunk getChunk(Entity self){
        return getLoc(self).getChunk();
    }

    public static double getZ(Entity self){
        return getLoc(self).getZ();
    }

    public static int getBx(Entity self){
        return getLoc(self).getBlockX();
    }

    public static int getBy(Entity self){
        return getLoc(self).getBlockY();
    }

    public static int getBz(Entity self){
        return getLoc(self).getBlockZ();
    }

    public static List<Entity> near(Entity self, double radius){
        Location loc = getLoc(self);
        List<Entity> entities = loc.getWorld().getEntities();
        Iterator<Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity.getLocation().distance(loc) > radius) iterator.remove();
        }
        return entities;
    }

    // setters

    public static void setBl(Entity self, int id){
        getBl(self).setTypeId(id);
    }

    public static void setBl(Entity self, Material id){
        getBl(self).setType(id);
    }

    public static void setBlock(Entity self, int id){
        setBl(self, id);
    }

    public static void setBlock(Entity self, Material id){
        setBl(self, id);
    }

    public static void bl(Entity self, Material id, int data){
        getBl(self).setTypeIdAndData(id.getId(), (byte)data, false);
    }

    public static void bl(Entity self, int id, int data){
        getBl(self).setTypeIdAndData(id, (byte)data, false);
    }

    public static void block(Entity self, Material id, int data){
        bl(self, id, data);
    }

    public static void block(Entity self, int id, int data){
        bl(self, id, data);
    }

    // region

    public static SphereRegion sphere(Entity self, double radius){
        return new SphereRegion(getLoc(self), radius);
    }

    public static SphereArea sphereArea(Entity self, double radius){
        return new SphereArea(getLoc(self), radius);
    }

    public static BoxRegion box(Entity self, double radius){
        return new BoxRegion(getLoc(self), radius, radius, radius);
    }

    public static BoxRegion box(Entity self, double x, double y, double z){
        return new BoxRegion(getLoc(self), x, y, z);
    }

    public static BoxArea boxArea(Entity self, double radius){
        return new BoxArea(getLoc(self), radius, radius);
    }

    public static BoxArea boxArea(Entity self, double x, double z){
        return new BoxArea(getLoc(self), x, z);
    }

    public static BoxRegion box(Entity self, Location other){
        return new BoxRegion(getLoc(self), other);
    }

    public static BoxRegion box(Entity self, Block other){
        return new BoxRegion(getLoc(self), other.getLocation());
    }

    public static BoxRegion box(Entity self, Entity other){
        return new BoxRegion(getLoc(self), other.getLocation());
    }

    public static BoxArea boxArea(Entity self, Location other){
        return new BoxArea(getLoc(self), other);
    }

    public static BoxArea boxArea(Entity self, Block other){
        return new BoxArea(getLoc(self), other.getLocation());
    }

    public static BoxArea boxArea(Entity self, Entity other){
        return new BoxArea(getLoc(self), other.getLocation());
    }

    // teleport

    public static Entity tphere(Entity self, Entity entity){
        Location loc = entity.getLocation();
        Location target = getLoc(self);
        target.setPitch(loc.getPitch());
        target.setYaw(loc.getYaw());
        entity.teleport(target);
        return self;
    }

    public static Entity leftShift(Entity self, Entity entity){
        return tphere(self, entity);
    }

    // spawn

    public static Entity spawn(Entity self, Class type){
        Location loc = getLoc(self);
        return loc.getWorld().spawn(loc, type);
    }

    public static Item spawn(Entity self, ItemStack itemStack){
        Location loc = getLoc(self);
        return loc.getWorld().dropItem(loc, itemStack);
    }

    public static FallingBlock spawn(Entity self, Material material, int data){
        Location loc = getLoc(self);
        return loc.getWorld().spawnFallingBlock(loc, material, (byte) data);
    }

    public static FallingBlock spawn(Entity self, Material material){
        return spawn(self, material, 0);
    }

    public static FallingBlock spawn(Entity self, int type, int data){
        Location loc = getLoc(self);
        return loc.getWorld().spawnFallingBlock(loc, type, (byte) data);
    }

    // effect

    public static Entity explode(Entity self, double power){
        Location loc = getLoc(self);
        loc.getWorld().createExplosion(loc, (float)power);
        return self;
    }

    public static Entity ex(Entity self, double power){
        return explode(self, power);
    }

    public static LightningStrike bolt(Entity self){
        Location loc = getLoc(self);
        return loc.getWorld().strikeLightning(loc);
    }

    public static LightningStrike fbolt(Entity self){
        Location loc = getLoc(self);
        return loc.getWorld().strikeLightningEffect(loc);
    }

    public static Entity sound(Entity self, Sound sound, double vol, double pitch){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, (float)vol, (float)pitch);
        return self;
    }

    public static Entity sound(Entity self, Sound sound, double vol){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, (float)vol, 1);
        return self;
    }

    public static Entity sound(Entity self, Sound sound){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, 1, 1);
        return self;
    }

    // relative

    public static Location up(Entity self, double len){
        Location loc = getLoc(self);
        loc.setY( loc.getY() + len );
        return loc;
    }

    public static Location up(Entity self){
        return up(self, 1);
    }

    public static Location down(Entity self, double len){
        Location loc = getLoc(self);
        loc.setY( loc.getY() - len );
        return loc;
    }

    public static Location down(Entity self){
        return down(self, 1);
    }

    public static Location move(Entity self, double x, double y, double z){
        Location loc = getLoc(self);
        return loc.add(x, y, z);
    }

    public static Location move(Entity self, Vector vector){
        Location loc = getLoc(self);
        return loc.add(vector);
    }

    public static Block rel(Entity self, int x, int y, int z) {
        return getBl(self).getRelative(x, y, z);
    }

    public static Block rel(Entity self, Vector vector) {
        return getBl(self).getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static double dist(Entity self, Location other){
        return getLoc(self).distance(other);
    }

    public static double dist(Entity self, Entity other){
        return getLoc(self).distance(other.getLocation());
    }

    public static double dist(Entity self, Block other){
        return getLoc(self).distance(other.getLocation());
    }

    public static Vector to(Entity self, Location val){
        Location loc = getLoc(self);
        if (val == null) return new Vector();
        if (!self.getWorld().equals(val.getWorld())) return null;
        return new Vector(val.getX()-loc.getX(), val.getY()-loc.getY(), val.getZ()-loc.getZ());
    }

    public static Vector to(Entity self, Entity val){
        return to(self, val.getLocation());
    }

    public static Vector to(Entity self, Block val){
        return to(self, val.getLocation());
    }

    public static Vector minus(Entity self, Location val){
        if (val == null) return new Vector();
        Location loc = getLoc(self);
        if (!loc.getWorld().equals(val.getWorld())) return null;
        return new Vector(loc.getX()-val.getX(), loc.getY()-val.getY(), loc.getZ()-val.getZ());
    }

    public static Vector minus(Entity self, Entity val){
        return minus(self, val.getLocation());
    }

    public static Vector minus(Entity self, Block val){
        return minus(self, val.getLocation());
    }

    // entity-specific

    public static Entity tpto(Entity self, Location target){
        Location loc = getLoc(self);
        target = target.clone();
        target.setPitch(loc.getPitch());
        target.setYaw(loc.getYaw());
        self.teleport(target);
        return self;
    }

    public static Entity rightShift(Entity self, Location target){
        return tpto(self, target);
    }

    public static Entity tp(Entity self, Location target){
        self.teleport(target);
        return self;
    }

    public static Entity tpto(Entity self, Entity target){
        return tpto(self, target.getLocation());
    }

    public static Entity rightShift(Entity self, Entity target){
        return tpto(self, target);
    }

    public static Entity tp(Entity self, Entity target){
        self.teleport(target);
        return self;
    }

    public static Entity tpto(Entity self, Block target){
        return tpto(self, target.getLocation().add(0.5, 0, 0.5));
    }

    public static Entity rightShift(Entity self, Block target){
        return tpto(self, target);
    }

    public static Entity tp(Entity self, Block target){
        self.teleport(target.getLocation().add(0.5, 0, 0.5));
        return self;
    }

    public static Entity shift(Entity self, Vector tar) {
        Location target = self.getLocation();
        target.add(tar);
        self.teleport(target);
        return self;
    }

    public static Entity shift(Entity self, double x, double y, double z) {
        Location target = self.getLocation();
        target.add(x, y, z);
        self.teleport(target);
        return self;
    }

    public static Entity shift(Entity self, double y) {
        return shift(self, 0, y, 0);
    }

    public static Entity forward(Entity self, double len) {
        Location loc = self.getLocation();
        return shift( self, loc.getDirection().multiply(len) );
    }

    public static Vector getVel(Entity self){
        return self.getVelocity();
    }

    public static Entity setVel(Entity self, Vector vel){
        self.setVelocity(vel);
        return self;
    }

    public static Entity th(Entity self, Vector v){
        return setVel(self, self.getVelocity().add(v) );
    }

    public static Entity th(Entity self, double x, double y, double z){
        return th(self, new Vector(x,y,z) );
    }

    public static Entity th(Entity self, double y){
        Vector vel = self.getVelocity();
        vel.setY( vel.getY() + y );
        return setVel(self, vel);
    }

    public static Entity vel(Entity self, Vector vel){
        return setVel(self, vel);
    }

    public static Entity setVel(Entity self, double len){
        return setVel(self, self.getLocation().getDirection().multiply(len));
    }

    public static String getCname(Entity self) {
        return self.getCustomName();
    }

    public static void setCname(Entity self, String name) {
        self.setCustomName(name);
    }

    public static int getFire(Entity self) {
        return self.getFireTicks();
    }

    public static void setFire(Entity self, int ticks) {
        self.setFireTicks(ticks);
    }

    public static float getFall(Entity self) {
        return self.getFallDistance();
    }

    public static void setFall(Entity self, float dist) {
        self.setFallDistance(dist);
    }

    public static Entity getPas(Entity self){
        return self.getPassenger();
    }

    public static void setPas(Entity self, Entity passenger){
        self.setPassenger(passenger);
    }

    public static void pas(Entity self, Entity passenger){
        setPas(self, passenger);
    }

    public static Entity getVeh(Entity self){
        return self.getVehicle();
    }

    public static void setVeh(Entity self, Entity vehicle){
        vehicle.setPassenger(self);
    }

    public static void veh(Entity self, Entity vehicle){
        setVeh(self, vehicle);
    }

    public static int getId(Entity self){
        return self.getEntityId();
    }

    public static void rm(Entity self){
        if (!(self instanceof Player)) self.remove();
    }

}
