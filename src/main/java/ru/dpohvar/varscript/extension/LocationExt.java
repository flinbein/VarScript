package ru.dpohvar.varscript.extension;

import org.bukkit.Location;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.dpohvar.varscript.extension.region.BoxArea;
import ru.dpohvar.varscript.extension.region.BoxRegion;
import ru.dpohvar.varscript.extension.region.SphereArea;
import ru.dpohvar.varscript.extension.region.SphereRegion;

import java.util.Iterator;
import java.util.List;

public class LocationExt {

    // getters

    public static Location getLoc(Location self){
        return self.clone();
    }

    public static Vector getDir(Location self){
        return getLoc(self).getDirection();
    }

    public static Block getBl(Location self){
        return getLoc(self).getBlock();
    }

    public static int getBx(Location self){
        return self.getBlockX();
    }

    public static int getBy(Location self){
        return self.getBlockY();
    }

    public static int getBz(Location self){
        return self.getBlockZ();
    }

    public static List<Entity> near(Location self, double radius){
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

    public static void setBx(Location self, int val){
        self.setX(val);
    }

    public static void setBy(Location self, int val){
        self.setY(val);
    }

    public static void setBz(Location self, int val){
        self.setZ(val);
    }

    public static void setBl(Location self, int id){
        getBl(self).setTypeId(id);
    }

    public static void setBl(Location self, Material id){
        getBl(self).setType(id);
    }

    public static void setBlock(Location self, int id){
        setBl(self, id);
    }

    public static void setBlock(Location self, Material id){
        setBl(self, id);
    }

    public static void bl(Location self, Material id, int data){
        getBl(self).setTypeIdAndData(id.getId(), (byte)data, false);
    }

    public static void bl(Location self, int id, int data){
        getBl(self).setTypeIdAndData(id, (byte)data, false);
    }

    public static void block(Location self, Material id, int data){
        bl(self, id, data);
    }

    public static void block(Location self, int id, int data){
        bl(self, id, data);
    }

    // region

    public static SphereRegion sphere(Location self, double radius){
        return new SphereRegion(getLoc(self), radius);
    }

    public static SphereArea sphereArea(Location self, double radius){
        return new SphereArea(getLoc(self), radius);
    }

    public static BoxRegion box(Location self, double radius){
        return new BoxRegion(getLoc(self), radius, radius, radius);
    }

    public static BoxRegion box(Location self, double x, double y, double z){
        return new BoxRegion(getLoc(self), x, y, z);
    }

    public static BoxArea boxArea(Location self, double radius){
        return new BoxArea(getLoc(self), radius, radius);
    }

    public static BoxArea boxArea(Location self, double x, double z){
        return new BoxArea(getLoc(self), x, z);
    }

    public static BoxRegion box(Location self, Location other){
        return new BoxRegion(getLoc(self), other);
    }

    public static BoxRegion box(Location self, Block other){
        return new BoxRegion(getLoc(self), other.getLocation());
    }

    public static BoxRegion box(Location self, Entity other){
        return new BoxRegion(getLoc(self), other.getLocation());
    }

    public static BoxArea boxArea(Location self, Location other){
        return new BoxArea(getLoc(self), other);
    }

    public static BoxArea boxArea(Location self, Block other){
        return new BoxArea(getLoc(self), other.getLocation());
    }

    public static BoxArea boxArea(Location self, Entity other){
        return new BoxArea(getLoc(self), other.getLocation());
    }

    // teleport

    public static Location tphere(Location self, Entity entity){
        Location loc = entity.getLocation();
        Location target = getLoc(self);
        target.setPitch(loc.getPitch());
        target.setYaw(loc.getYaw());
        entity.teleport(target);
        return self;
    }

    public static Location leftShift(Location self, Entity entity){
        return tphere(self, entity);
    }

    // spawn

    public static <T extends Entity> T spawn(Location self, Class<T> type){
        Location loc = getLoc(self);
        return loc.getWorld().spawn(loc, type);
    }

    public static Item spawn(Location self, ItemStack itemStack){
        Location loc = getLoc(self);
        return loc.getWorld().dropItem(loc, itemStack);
    }

    public static FallingBlock spawn(Location self, Material material, int data){
        Location loc = getLoc(self);
        return loc.getWorld().spawnFallingBlock(loc, material, (byte) data);
    }

    public static FallingBlock spawn(Location self, Material material){
        return spawn(self, material, 0);
    }

    public static FallingBlock spawn(Location self, int type, int data){
        Location loc = getLoc(self);
        return loc.getWorld().spawnFallingBlock(loc, type, (byte) data);
    }

    // effect

    public static Location explode(Location self, double power){
        Location loc = getLoc(self);
        loc.getWorld().createExplosion(loc, (float)power);
        return self;
    }

    public static Location ex(Location self, double power){
        return explode(self, power);
    }

    public static LightningStrike bolt(Location self){
        Location loc = getLoc(self);
        return loc.getWorld().strikeLightning(loc);
    }

    public static LightningStrike fbolt(Location self){
        Location loc = getLoc(self);
        return loc.getWorld().strikeLightningEffect(loc);
    }

    public static Location sound(Location self, Sound sound, double vol, double pitch){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, (float)vol, (float)pitch);
        return self;
    }

    public static Location sound(Location self, Sound sound, double vol){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, (float)vol, 1);
        return self;
    }

    public static Location sound(Location self, Sound sound){
        Location loc = getLoc(self);
        loc.getWorld().playSound(loc, sound, 1, 1);
        return self;
    }

    // relative

    public static Location up(Location self, double len){
        Location loc = getLoc(self);
        loc.setY( loc.getY() + len );
        return loc;
    }

    public static Location up(Location self){
        return up(self, 1);
    }

    public static Location down(Location self, double len){
        Location loc = getLoc(self);
        loc.setY( loc.getY() - len );
        return loc;
    }

    public static Location down(Location self){
        return down(self, 1);
    }

    public static Location move(Location self, double x, double y, double z){
        Location loc = getLoc(self);
        return loc.add(x, y, z);
    }

    public static Location move(Location self, Vector vector){
        Location loc = getLoc(self);
        return loc.add(vector);
    }

    public static Block rel(Location self, int x, int y, int z) {
        return getBl(self).getRelative(x, y, z);
    }

    public static Block rel(Location self, Vector vector) {
        return getBl(self).getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static double dist(Location self, Location other){
        return getLoc(self).distance(other);
    }

    public static double dist(Location self, Entity other){
        return getLoc(self).distance(other.getLocation());
    }

    public static double dist(Location self, Block other){
        return getLoc(self).distance(other.getLocation());
    }

    public static Vector to(Location self, Location val){
        Location loc = getLoc(self);
        if (val == null) return new Vector();
        if (!self.getWorld().equals(val.getWorld())) return null;
        return new Vector(val.getX()-loc.getX(), val.getY()-loc.getY(), val.getZ()-loc.getZ());
    }

    public static Vector to(Location self, Entity val){
        return to(self, val.getLocation());
    }

    public static Vector to(Location self, Block val){
        return to(self, val.getLocation());
    }

    public static Vector minus(Location self, Location val){
        if (val == null) return new Vector();
        Location loc = getLoc(self);
        if (!loc.getWorld().equals(val.getWorld())) return null;
        return new Vector(loc.getX()-val.getX(), loc.getY()-val.getY(), loc.getZ()-val.getZ());
    }

    public static Vector minus(Location self, Entity val){
        return minus(self, val.getLocation());
    }

    public static Vector minus(Location self, Block val){
        return minus(self, val.getLocation());
    }

    public static Location plus(Location self, Vector val){
        return getLoc(self).add(val);
    }

    // location-specific

    public static Location add(Location self, Vector vector){
        return self.add(vector);
    }

    public static Location mid(Location self, Location val){
        return new Location(
                self.getWorld(),
                self.getX() + (val.getX() - self.getX()) / 2,
                self.getY() + (val.getY() - self.getY()) / 2,
                self.getZ() + (val.getZ() - self.getZ()) / 2
        );
    }

    public static Location or(Location self, Location val){
        return mid(self,val);
    }

}

























