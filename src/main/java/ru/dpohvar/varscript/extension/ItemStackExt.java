package ru.dpohvar.varscript.extension;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ItemStackExt {

    public static <T extends ItemStack> T rightShift(T self, InventoryHolder holder) {
        holder.getInventory().addItem(self);
        return self;
    }

    public static <T extends ItemStack> T rightShift(T self, Block holder) {
        ((InventoryHolder)holder.getState()).getInventory().addItem(self);
        return self;
    }

    public static ItemStack multiply(ItemStack self, int amount){
        ItemStack itemStack = self.clone();
        itemStack.setAmount(itemStack.getAmount() * amount);
        return itemStack;
    }

    public static ItemStack power(ItemStack self, int amount){
        ItemStack itemStack = self.clone();
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static ItemStack plus(ItemStack self, int amount){
        ItemStack itemStack = self.clone();
        itemStack.setAmount(itemStack.getAmount() + amount);
        return itemStack;
    }

    public static ItemStack minus(ItemStack self, int amount){
        ItemStack itemStack = self.clone();
        itemStack.setAmount(itemStack.getAmount() - amount);
        return itemStack;
    }

    public static ItemStack next(ItemStack self){
        ItemStack itemStack = self.clone();
        itemStack.setAmount(self.getAmount()+1);
        return itemStack;
    }

    public static ItemStack previous(ItemStack self){
        ItemStack itemStack = self.clone();
        itemStack.setAmount(self.getAmount()-1);
        return itemStack;
    }

}
