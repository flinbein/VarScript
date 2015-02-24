package ru.dpohvar.varscript.extension.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BoxArea extends Region {
    protected double x1, z1;
    protected double x2, z2;
    protected World world;

    public BoxArea(Location locA, Location locB) {
        x1 = locA.getX();
        z1 = locA.getZ();
        x2 = locB.getX();
        z2 = locB.getZ();
        if (x1 > x2) {
            Double t = x2;
            x2 = x1;
            x1 = t;
        }
        if (z1 > z2) {
            Double t = z2;
            z2 = z1;
            z1 = t;
        }
        world = locA.getWorld();
    }

    public String toString() {
        return "CUBEAREA(" + x1 + ":" + z1 + "," + x2 + ":" + z2 + "," + world.getName() + ")";
    }

    public BoxArea(Location loc, double distX, double distZ) {
        distX = Math.abs(distX);
        distZ = Math.abs(distZ);
        x1 = loc.getX() - distX / 2;
        z1 = loc.getZ() - distZ / 2;
        x2 = loc.getX() + distX / 2;
        z2 = loc.getZ() + distZ / 2;
        world = loc.getWorld();
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Location getCenter() {
        return new Location(world, (x1 + x2) / 2, world.getHighestBlockYAt((int) (x1 + x2) / 2, (int) (z1 + z2) / 2), (z1 + z2) / 2);
    }

    @Override
    public boolean contains(Location l) {
        if (!l.getWorld().equals(world)) return false;
        if (x2 < l.getX() || l.getX() < x1) return false;
        if (z2 < l.getZ() || l.getZ() < z1) return false;
        return true;
    }

    @Override
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<Block>();
        int xa = (int) Math.floor(x1), za = (int) Math.floor(z1);
        int xb = (int) Math.floor(x2), zb = (int) Math.floor(z2);
        for (int x = xa; x < xb; x++)
            for (int z = za; z < zb; z++) {
                blocks.add(world.getHighestBlockAt(x, z));
            }
        return blocks;
    }

    @Override
    public List<Block> getBorder() {
        List<Block> blocks = new ArrayList<Block>();
        int xa = (int) (Math.floor(x1)), za = (int) (Math.floor(z1));
        int xb = (int) (Math.floor(x2)), zb = (int) (Math.floor(z2));
        for (int x = xa; x <= xb; x++)
            for (int z = za; z <= zb; z++) {
                if (x == xa || x == xb - 1 || z == za || z == zb - 1) {
                    blocks.add(world.getHighestBlockAt(x, z));
                }
            }
        return blocks;
    }

    @Override
    public BoxArea clone() {
        BoxArea c = new BoxArea();
        c.x1 = x1;
        c.z1 = z1;
        c.x2 = x2;
        c.z2 = z2;
        c.world = world;
        return c;
    }

    protected BoxArea() {
    }
}