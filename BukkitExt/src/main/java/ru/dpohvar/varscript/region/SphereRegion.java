package ru.dpohvar.varscript.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class SphereRegion extends Region {
    protected double px, py, pz;
    protected double radius;
    protected World world;

    public String toString() {
        return "SPHEREREGION(" + px + ":" + py + ":" + pz + "," + radius + "," + world.getName() + ")";
    }

    public SphereRegion(Location loc, double rad) {
        rad = Math.abs(rad);
        px = loc.getX();
        py = loc.getY();
        pz = loc.getZ();
        radius = rad;
        world = loc.getWorld();
    }

    @Override
    public SphereRegion clone() {
        SphereRegion c = new SphereRegion();
        c.px = px;
        c.py = py;
        c.pz = pz;
        c.radius = radius;
        c.world = world;
        return c;
    }

    protected SphereRegion() {
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Location getCenter() {
        return new Location(world, px, py, pz);
    }

    @Override
    public boolean containsLocation(Location l) {
        if (!l.getWorld().equals(world)) return false;
        return (l.distance(getCenter()) <= radius);
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<Block>();
        int xa = (int) (px - radius - 1), ya = (int) (py - radius - 1), za = (int) (pz - radius - 1);
        int xb = (int) (px + radius + 1), yb = (int) (py + radius + 1), zb = (int) (pz + radius + 1);
        for (int x = xa; x < xb; x++)
            for (int y = ya; y < yb; y++)
                for (int z = za; z < zb; z++) {
                    if ((px - x) * (px - x) + (py - y) * (py - y) + (pz - z) * (pz - z) < radius * radius)
                        blocks.add(world.getBlockAt(x, y, z));
                }
        return blocks;
    }

    @Override
    public List<Block> getBorder() {
        List<Block> blocks = new ArrayList<Block>();
        int xa = (int) (px - radius - 1), ya = (int) (py - radius - 1), za = (int) (pz - radius - 1);
        int xb = (int) (px + radius + 1), yb = (int) (py + radius + 1), zb = (int) (pz + radius + 1);
        for (int x = xa; x < xb; x++)
            for (int y = ya; y < yb; y++)
                for (int z = za; z < zb; z++) {
                    if (
                            (px - x) * (px - x) + (py - y) * (py - y) + (pz - z) * (pz - z) < radius * radius &&
                                    (px - x) * (px - x) + (py - y) * (py - y) + (pz - z) * (pz - z) > (radius - Math.sqrt(3)) * (radius - Math.sqrt(3))
                            ) blocks.add(world.getBlockAt(x, y, z));
                }
        return blocks;
    }
}
