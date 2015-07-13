package ru.dpohvar.varscript.extension;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.dpohvar.varscript.workspace.CallerScript;

import java.io.*;

public class CallerScriptExt {

    public static Damageable kill(CallerScript self, Damageable damageable) {
        damageable.setHealth(0);
        return damageable;
    }

    public static Damageable heal(CallerScript self, Damageable damageable) {
        damageable.setHealth( damageable.getMaxHealth() );
        return damageable;
    }

    public static Player kick(CallerScript self, Player player, String message) {
        player.kickPlayer(message);
        return player;
    }

    public static Player kick(CallerScript self, Player player) {
        return kick(self, player, "");
    }

    public static Entity spawn(CallerScript self, Class type) {
        Object me = self.getMe();
        Location loc = (Location) InvokerHelper.getProperty(me, "location");
        return loc.getWorld().spawn(loc, type);
    }

    public static Item spawn(CallerScript self, ItemStack itemStack){
        Object me = self.getMe();
        Location loc = (Location) InvokerHelper.getProperty(me, "location");
        return loc.getWorld().dropItem(loc, itemStack);
    }

    public static FallingBlock spawn(CallerScript self, Material material, int data){
        Object me = self.getMe();
        Location loc = (Location) InvokerHelper.getProperty(me, "location");
        return loc.getWorld().spawnFallingBlock(loc, material, (byte) data);
    }

    public static FallingBlock spawn(CallerScript self, Material material){
        return spawn(self, material, 0);
    }

    public static FallingBlock spawn(CallerScript self, int type, int data){
        Object me = self.getMe();
        Location loc = (Location) InvokerHelper.getProperty(me, "location");
        return loc.getWorld().spawnFallingBlock(loc, type, (byte) data);
    }

    public static void explode(CallerScript self, Location loc, double power){
        loc.getWorld().createExplosion(loc, (float)power);
    }

    public static void explode(CallerScript self, Block block, double power){
        explode(self, block.getLocation().add(0.5,0.5,0.5), power);
    }

    public static void explode(CallerScript self, Entity entity, double power){
        explode(self, entity.getLocation(), power);
    }

    public static void ex(CallerScript self, Location loc, double power){
        explode(self, loc, power);
    }

    public static void ex(CallerScript self, Block block, double power){
        explode(self, block, power);
    }

    public static void ex(CallerScript self, Entity entity, double power){
        explode(self, entity, power);
    }

    public static void tpto(CallerScript self, Location loc){
        ((Entity) self.getMe()).teleport(loc);
    }

    public static void tpto(CallerScript self, Entity entity){
        ((Entity) self.getMe()).teleport(entity);
    }

    public static void tpto(CallerScript self, Block block){
        ((Entity) self.getMe()).teleport(block.getLocation().add(0.5,0,0.5));
    }

    public static <T extends Entity> T tphere(CallerScript self, T entity){
        Object me = self.getMe();
        Location loc = (Location) InvokerHelper.getProperty(me, "location");
        entity.teleport(loc);
        return entity;
    }

    public static Entity id(CallerScript self, int id) throws IllegalArgumentException{
        for (World world : self.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getEntityId() == id) return entity;
            }
        }
        throw new IllegalArgumentException("no entity with id "+id);
    }

    public static String toJSON (CallerScript self, Object src){
        return org.json.simple.JSONValue.toJSONString(src);
    }

    public static Object parseJSON (CallerScript self, CharSequence src){
        return org.json.simple.JSONValue.parse(src.toString());
    }

    public static Object parseJSON (CallerScript self, File file) throws FileNotFoundException{
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            Reader reader = new InputStreamReader(stream,"UTF8");
            return org.json.simple.JSONValue.parse(reader);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (stream != null) try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
