package ru.dpohvar.varscript.region;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ru.dpohvar.varscript.utils.EnumUtils;

import java.util.*;

public abstract class Region implements Cloneable {

    abstract public World getWorld();

    abstract public Location getCenter();

    abstract public boolean containsLocation(Location l);

    abstract public List<Block> getBlocks();

    abstract public List<Block> getBorder();

    public List<Block> getSolidBlocks(){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        while(itr.hasNext()) {
            if (itr.next().isEmpty()) {
                itr.remove();
            }
        }
        return blocks;
    }

    public List<Block> getEmptyBlocks(){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        while(itr.hasNext()) {
            if (!itr.next().isEmpty()) {
                itr.remove();
            }
        }
        return blocks;
    }

    public List<Block> getLiquidBlocks(){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        while(itr.hasNext()) {
            if (!itr.next().isLiquid()) {
                itr.remove();
            }
        }
        return blocks;
    }

    public List<Block> getBlocksOfType(Material type){
        List<Block> blocks = getBlocks();
        Iterator<Block> itr = blocks.iterator();
        for (;;) {
            if (!itr.hasNext()) return blocks;
            if (!itr.next().getType().equals(type)) {
                itr.remove();
            }
        }
    }

    public List<Block> getBlocksOfType(int type){
        return getBlocksOfType(Material.getMaterial(type));
    }

    public List<Block> getBlocksOfType(String type){
        return getBlocksOfType(EnumUtils.match(Material.values(), type));
    }

    public List<Entity> getEntities(){
        List<Entity> result = new ArrayList<>();
        for (Entity e: getWorld().getEntities()) {
            if (containsLocation(e.getLocation())) result.add(e);
        }
        return result;
    }

    public List<Entity> getEntitiesOfType(EntityType type){
        List<Entity> result = new ArrayList<>();
        for (Entity e: getWorld().getEntities()) {
            if (e.getType().equals(type)) {
                if (containsLocation(e.getLocation())) result.add(e);
            }
        }
        return result;
    }

    public List<Entity> getEntitiesOfType(String type){
        return getEntitiesOfType(EnumUtils.match(EntityType.values(), type));
    }

    public List<Entity> getLivingEntities(){
        List<Entity> result = new ArrayList<>();
        for (Entity e: getWorld().getLivingEntities()) {
            if (containsLocation(e.getLocation())) result.add(e);
        }
        return result;
    }

    public List<Entity> getPlayers(){
        return getEntitiesOfType(EntityType.PLAYER);
    }


}
