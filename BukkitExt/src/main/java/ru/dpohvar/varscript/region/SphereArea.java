package ru.dpohvar.varscript.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class SphereArea extends Region {
    protected double px, pz;
    protected double radius;
    protected World world;

    public String toString() {
        return "SPHEREAREA(" + px + ":" + pz + "," + radius + "," + world.getName() + ")";
    }

    @Override
    public SphereArea clone() {
        SphereArea c = new SphereArea();
        c.px = px;
        c.pz = pz;
        c.radius = radius;
        c.world = world;
        return c;
    }

    protected SphereArea() {
    }

    public SphereArea(Location loc, double rad) {
        rad = Math.abs(rad);
        px = loc.getX();
        pz = loc.getZ();
        radius = rad;
        world = loc.getWorld();
    }


    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Location getCenter() {
        return new Location(world, px, world.getHighestBlockYAt((int) px, (int) pz), pz);
    }

    @Override
    public boolean containsLocation(Location l) {
        if (!l.getWorld().equals(world)) return false;
        return l.distance(new Location(world, px, l.getY(), pz)) <= radius;
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<Block>();
        int xa = (int) Math.floor(px - radius), za = (int) Math.floor(px - radius);
        int xb = (int) Math.floor(px + radius), zb = (int) Math.floor(px + radius);
        for (int x = xa; x <= xb; x++)
            for (int z = za; z <= zb; z++) {
                if ((px - x) * (px - x) + (pz - z) * (pz - z) < radius * radius)
                    blocks.add(world.getHighestBlockAt(x, z));
            }
        return blocks;
    }

    @Override
    public List<Block> getBorder() {
        List<Block> blocks = new ArrayList<Block>();
        int xa = (int) Math.floor(px - radius), za = (int) Math.floor(px - radius);
        int xb = (int) Math.floor(px + radius), zb = (int) Math.floor(px + radius);
        for (int x = xa; x <= xb; x++)
            for (int z = za; z <= zb; z++) {
                if (
                        (px - x) * (px - x) + (pz - z) * (pz - z) < radius * radius &&
                                (px - x) * (px - x) + (pz - z) * (pz - z) > (radius - Math.sqrt(2)) * (radius - Math.sqrt(2))
                        ) {
                    Block b = world.getHighestBlockAt(x, z);
                    for (int i = 0; i < 5; i++) if (b.getY() + i < world.getMaxHeight()) blocks.add(b);
                }
            }
        return blocks;
    }
}
