package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import ru.dpohvar.varscript.groovy.BukkitExtUtils

/**
 * Created by DPOH-VAR on 06.03.14
 */

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class InventoryHolderExtension<Z extends InventoryHolder> {

    public static Z give(Z self, ItemStack item) {
        self.inventory.addItem item
        self
    }

    public static Z give(Z self, int id, int data=0, int amount=1) {
        self.inventory.addItem BukkitExtUtils.item(id, data, amount)
        self
    }

    public static Z give(Z self, Material id, int data=0, int amount=1) {
        self.inventory.addItem BukkitExtUtils.item(id, data, amount)
        self
    }

    public static Z give(Z self, String id, int data=0, int amount=1) {
        self.inventory.addItem BukkitExtUtils.item(id, data, amount)
        self
    }

    public static Z clear(Z self) {
        self.inventory.clear()
        if (self instanceof LivingEntity) self.equipment.clear()
        self
    }

    public static List<ItemStack> getItems(Z self) {
        self.inventory.contents as List
    }

    public static Z setItems(Z self, List<ItemStack> con) {
        ItemStack[] conArray = new ItemStack[self.inventory.size]
        int i=0
        for (def item in con) conArray[i++] = item
        self.inventory.contents = conArray
        self
    }

    public static def items(Z self, Closure closure) {
        def items = getItems self
        def it = items.clone() as List
        def result = closure it
        if (items != it) setItems self, it
        result
    }

}
