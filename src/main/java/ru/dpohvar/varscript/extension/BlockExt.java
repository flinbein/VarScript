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

public class BlockExt {

    // getters

    public static Location getLoc(Block self){
        return self.getLocation();
    }

    public static Vector getDir(Block self){
        return getLoc(self).getDirection();
    }

    public static Block getBl(Block self){
        return getLoc(self).getBlock();
    }

    public static double getX(Block self){
        return getLoc(self).getX();
    }

    public static double getY(Block self){
        return getLoc(self).getY();
    }

    public static float getPitch(Block self){
        return getLoc(self).getPitch();
    }

    public static float getYaw(Block self){
        return getLoc(self).getYaw();
    }

    public static Chunk getChunk(Block self){
        return getLoc(self).getChunk();
    }

    public static double getZ(Block self){
        return getLoc(self).getZ();
    }

    public static int getBx(Block self){
        return getLoc(self).getBlockX();
    }

    public static int getBy(Block self){
        return getLoc(self).getBlockY();
    }

    public static int getBz(Block self){
        return getLoc(self).getBlockZ();
    }

    public static List<Entity> near(Block self, double radius){
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

    public static void setBl(Block self, int id){
        getBl(self).setTypeId(id);
    }

    public static void setBl(Block self, Material id){
        getBl(self).setType(id);
    }

    public static void setBlock(Block self, int id){
        setBl(self, id);
    }

    public static void setBlock(Block self, Material id){
        setBl(self, id);
    }

    public static void bl(Block self, Material id, int data){
        getBl(self).setTypeIdAndData(id.getId(), (byte)data, false);
    }

    public static void bl(Block self, int id, int data){
        getBl(self).setTypeIdAndData(id, (byte)data, false);
    }

    public static void block(Block self, Material id, int data){
        bl(self, id, data);
    }

    public static void block(Block self, int id, int data){
        bl(self, id, data);
    }

    // region

    public static SphereRegion sphere(Block self, double radius){
        return new SphereRegion(getLoc(self), radius);
    }

    public static SphereArea sphereArea(Block self, double radius){
        return new SphereArea(getLoc(self), radius);
    }

    public static BoxRegion box(Block self, double radius){
        return new BoxRegion(getLoc(self), radius, radius, radius);
    }

    public static BoxRegion box(Block self, double x, double y, double z){
        return new BoxRegion(getLoc(self), x, y, z);
    }

    public static BoxArea boxArea(Block self, double radius){
        return new BoxArea(getLoc(self), radius, radius);
    }

    public static BoxArea boxArea(Block self, double x, double z){
        return new BoxArea(getLoc(self), x, z);
    }

    public static BoxRegion box(Block self, Location other){
        return new BoxRegion(getLoc(self), other);
    }

    public static BoxRegion box(Block self, Block other){
        return new BoxRegion(getLoc(self), other.getLocation());
    }

    public static BoxRegion box(Block self, Entity other){
        return new BoxRegion(getLoc(self), other.getLocation());
    }

    public static BoxArea boxArea(Block self, Location other){
        return new BoxArea(getLoc(self), other);
    }

    public static BoxArea boxArea(Block self, Block other){
        return new BoxArea(getLoc(self), other.getLocation());
    }

    public static BoxArea boxArea(Block self, Entity other){
        return new BoxArea(getLoc(self), other.getLocation());
    }

    // teleport

    public static Block tphere(Block self, Entity entity){
        Location loc = entity.getLocation();
        Location target = getLoc(self);
        target.setPitch(loc.getPitch());
        target.setYaw(loc.getYaw());
        entity.teleport(target);
        return self;
    }

    public static Block leftShift(Block self, Entity entity){
        return tphere(self, entity);
    }

    // spawn

    public static Entity spawn(Block self, Class type){
        Location loc = getLoc(self);
        return loc.getWorld().spawn(loc, type);
    }

    public static Item spawn(Block self, ItemStack itemStack){
        Location loc = getLoc(self);
        return loc.getWorld().dropItem(loc, itemStack);
    }

    public static FallingBlock spawn(Block self, Material material, int data){
        Location loc = getLoc(self).add(0.5, 0, 0.5);
        return loc.getWorld().spawnFallingBlock(loc, material, (byte) data);
    }

    public static FallingBlock spawn(Block self, Material material){
        return spawn(self, material, 0);
    }

    public static FallingBlock spawn(Block self, int type, int data){
        Location loc = getLoc(self).add(0.5, 0, 0.5);
        return loc.getWorld().spawnFallingBlock(loc, type, (byte) data);
    }

    // effect

    public static Block explode(Block self, double power){
        Location loc = getLoc(self).add(0.5,0.5,0.5);
        loc.getWorld().createExplosion(loc, (float)power);
        return self;
    }

    public static Block ex(Block self, double power){
        return explode(self, power);
    }

    public static LightningStrike bolt(Block self){
        Location loc = getLoc(self);
        return loc.getWorld().strikeLightning(loc);
    }

    public static LightningStrike fbolt(Block self){
        Location loc = getLoc(self);
        return loc.getWorld().strikeLightningEffect(loc);
    }

    public static Block sound(Block self, Sound sound, double vol, double pitch){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, (float)vol, (float)pitch);
        return self;
    }

    public static Block sound(Block self, Sound sound, double vol){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, (float)vol, 1);
        return self;
    }

    public static Block sound(Block self, Sound sound){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, 1, 1);
        return self;
    }

    // relative

    public static Location up(Block self, double len){
        Location loc = getLoc(self);
        loc.setY( loc.getY() + len );
        return loc;
    }

    public static Location up(Block self){
        return up(self, 1);
    }

    public static Location down(Block self, double len){
        Location loc = getLoc(self);
        loc.setY( loc.getY() - len );
        return loc;
    }

    public static Location down(Block self){
        return down(self, 1);
    }

    public static Location move(Block self, double x, double y, double z){
        Location loc = getLoc(self);
        return loc.add(x, y, z);
    }

    public static Location move(Block self, Vector vector){
        Location loc = getLoc(self);
        return loc.add(vector);
    }

    public static Block rel(Block self, int x, int y, int z) {
        return getBl(self).getRelative(x, y, z);
    }

    public static Block rel(Block self, Vector vector) {
        return getBl(self).getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static double dist(Block self, Location other){
        return getLoc(self).distance(other);
    }

    public static double dist(Block self, Entity other){
        return getLoc(self).distance(other.getLocation());
    }

    public static double dist(Block self, Block other){
        return getLoc(self).distance(other.getLocation());
    }

    public static Vector to(Block self, Location val){
        Location loc = getLoc(self);
        if (val == null) return new Vector();
        if (!self.getWorld().equals(val.getWorld())) return null;
        return new Vector(val.getX()-loc.getX(), val.getY()-loc.getY(), val.getZ()-loc.getZ());
    }

    public static Vector to(Block self, Entity val){
        return to(self, val.getLocation());
    }

    public static Vector to(Block self, Block val){
        return to(self, val.getLocation());
    }

    public static Vector minus(Block self, Location val){
        if (val == null) return new Vector();
        Location loc = getLoc(self);
        if (!loc.getWorld().equals(val.getWorld())) return null;
        return new Vector(loc.getX()-val.getX(), loc.getY()-val.getY(), loc.getZ()-val.getZ());
    }

    public static Vector minus(Block self, Entity val){
        return minus(self, val.getLocation());
    }

    public static Vector minus(Block self, Block val){
        return minus(self, val.getLocation());
    }

    public static Location plus(Block self, Vector val){
        return getLoc(self).add(val);
    }

    // block-specific

    public static int getId(Block self){
        return self.getTypeId();
    }

    public static void setId(Block self, int value){
        self.setTypeId(value);
    }

    public static Block set(Block self, int type, int data, boolean physic){
        self.setTypeIdAndData(type, (byte)data, physic);
        return self;
    }

    public static Block set(Block self, int type, int data){
        self.setTypeIdAndData(type, (byte)data, false);
        return self;
    }

    public static Block set(Block self, int type){
        self.setTypeIdAndData(type, (byte)0, false);
        return self;
    }

}
