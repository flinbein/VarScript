package ru.dpohvar.varscript.extension;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class MaterialExt {

    public static Material rightShift(Material self, InventoryHolder holder) {
        holder.getInventory().addItem(new ItemStack(self));
        return self;
    }

    public static Material rightShift(Material self, Block holder) {
        ((InventoryHolder)holder.getState()).getInventory().addItem(new ItemStack(self));
        return self;
    }

    public static ItemStack multiply(Material self, int amount){
        ItemStack itemStack = new ItemStack(self);
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static ItemStack power(Material self, int amount){
        ItemStack itemStack = new ItemStack(self);
        itemStack.setAmount(amount);
        return itemStack;
    }

}
