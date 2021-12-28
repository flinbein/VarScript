package ru.dpohvar.varscript.extension.region;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class Region implements Cloneable, Iterable<Block> {

    abstract public World getWorld();

    abstract public Location getCenter();

    abstract public boolean contains(Location l);

    abstract public List<Block> getBlocks();

    abstract public List<Block> getBorder();

    public boolean contains(Entity entity){
        return contains(entity.getLocation());
    }

    public boolean contains(Block block){
        return getBlocks().contains(block);
    }

    public List<Block> getSolid(){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        while(itr.hasNext()) {
            if (itr.next().isEmpty()) {
                itr.remove();
            }
        }
        return blocks;
    }

    public List<Block> getAir(){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        while(itr.hasNext()) {
            if (!itr.next().isEmpty()) {
                itr.remove();
            }
        }
        return blocks;
    }

    public List<Block> getLiquid(){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        while(itr.hasNext()) {
            if (!itr.next().isLiquid()) {
                itr.remove();
            }
        }
        return blocks;
    }

    public List<Block> blocks(Material type){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        for (;;) {
            if (!itr.hasNext()) return blocks;
            if (!itr.next().getType().equals(type)) {
                itr.remove();
            }
        }
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public List<Block> blocks(String type){
        return blocks(Material.matchMaterial(type));
    }

    public List<Entity> getEntities(){
        List<Entity> result = new ArrayList<Entity>();
        for (Entity e: getWorld().getEntities()) {
            if (contains(e.getLocation())) result.add(e);
        }
        return result;
    }

    public List<Entity> entities(EntityType type){
        List<Entity> result = new ArrayList<Entity>();
        for (Entity e: getWorld().getEntities()) {
            if (e.getType().equals(type)) {
                if (contains(e.getLocation())) result.add(e);
            }
        }
        return result;
    }

    public List<Entity> entities(Class<?>... types){
        List<Entity> result = new ArrayList<Entity>();
        for (Entity e: getWorld().getEntitiesByClasses(types)) {
            if (contains(e.getLocation())) result.add(e);
        }
        return result;
    }

    public List<Entity> getLiving(){
        List<Entity> result = new ArrayList<Entity>();
        for (Entity e: getWorld().getLivingEntities()) {
            if (contains(e.getLocation())) result.add(e);
        }
        return result;
    }

    public List<Player> getPlayers(){
        List<Player> result = getWorld().getPlayers();
        Iterator<Player> iterator = result.iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (!contains(player)) iterator.remove();
        }
        return result;
    }

    public boolean isCase(Block block){
        return contains(block);
    }

    public boolean isCase(Location location){
        return contains(location);
    }

    public boolean isCase(Entity entity){
        return contains(entity);
    }

    @Override
    public Iterator<Block> iterator() {
        return getBlocks().iterator();
    }
}