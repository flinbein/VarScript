package ru.dpohvar.varscript.extension;

import groovy.lang.MissingMethodException;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class InventoryHolderExt {

    public static <T extends InventoryHolder> T leftShift(T self, Material mat) {
        self.getInventory().addItem(new ItemStack(mat));
        return self;
    }

    public static <T extends InventoryHolder> T leftShift(T self, ItemStack itemStack) {
        self.getInventory().addItem(itemStack);
        return self;
    }

    public static Block leftShift(Block block, Material mat) {
        if (block.getState() instanceof InventoryHolder) leftShift((InventoryHolder)block, mat);
        else throw new MissingMethodException("leftShift",Block.class,new Object[]{mat});
        return block;
    }

    public static Block leftShift(Block block, ItemStack itemStack) {
        BlockState state = block.getState();
        if (state instanceof InventoryHolder) leftShift((InventoryHolder) state, itemStack);
        else throw new MissingMethodException("leftShift",Block.class,new Object[]{itemStack});
        return block;
    }

    public static <T extends InventoryHolder> T give(T self, ItemStack... item) {
        self.getInventory().addItem(item);
        return self;
    }

    public static <T extends InventoryHolder> T give(T self, Material mat) {
        self.getInventory().addItem(new ItemStack(mat));
        return self;
    }

    public static <T extends InventoryHolder> T give(T self, String mat) {
        self.getInventory().addItem(new ItemStack(Material.matchMaterial(mat)));
        return self;
    }

    public static <T extends InventoryHolder> T give(T self, Material mat, int amount, int data) {
        self.getInventory().addItem(new ItemStack(mat, amount, (short)data));
        return self;
    }

    public static <T extends InventoryHolder> T give(T self, String mat, int amount, int data) {
        self.getInventory().addItem(new ItemStack(Material.matchMaterial(mat), amount, (short)data));
        return self;
    }

    public static <T extends InventoryHolder> T clear(T self) {
        self.getInventory().clear();
        if (self instanceof LivingEntity) ((LivingEntity) self).getEquipment().clear();
        return self;
    }

    public static List<ItemStack> getItems(InventoryHolder self) {
        return Arrays.asList(self.getInventory().getContents());
    }

    public static <T extends InventoryHolder> T setItems(T self, List<ItemStack> con) {
        ItemStack[] conArray = new ItemStack[self.getInventory().getSize()];
        int i = 0;
        for (ItemStack itemStack : con) conArray[i++] = itemStack;
        self.getInventory().setContents(conArray);
        return self;
    }
}
