package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import ru.dpohvar.varscript.groovy.BukkitExtUtils

/**
 * Created by DPOH-VAR on 06.03.14
 */

@SuppressWarnings(["GroovyUnusedDeclaration", "GrDeprecatedAPIUsage"])
@CompileStatic
class LivingEntityExtension<Z extends LivingEntity> {

    public static Block getTarb(Z self) {
        self.getTargetBlock null, 32
    }

    public static Location getEye(Z self) {
        self.eyeLocation
    }

    public static int getAir(Z self) {
        self.remainingAir
    }

    public static Z setAir(Z self, int air) {
        self.remainingAir = air
        self
    }

    public static boolean isLookAt(Z self, Z entity) {
        self.hasLineOfSight entity
    }

    public static Entity getTar(Z self) {
        self.world.entities.findAll { Entity it ->
            self.hasLineOfSight it
        }.sort { Entity it ->
            self.location.distance it.location
        }.reverse() [0]
    }

    public static boolean isPickup(Z self) {
        self.canPickupItems
    }

    public static Z setPickup(Z self, boolean v) {
        self.canPickupItems = v
        self
    }

    public static EntityEquipment getEq(Z self) {
        self.equipment
    }

    public static Entity getLeash(Z self) {
        self.leashHolder
    }

    public static Z setLeash(Z self, Entity holder) {
        self.leashHolder = holder
        self
    }

    public static ItemStack getBoots(Z self) {
        self.equipment.boots
    }

    public static ItemStack getHelmet(Z self) {
        self.equipment.helmet
    }

    public static ItemStack getLegs(Z self) {
        self.equipment.leggings
    }

    public static ItemStack getHand(Z self) {
        self.equipment.itemInHand
    }

    public static ItemStack getArmor(Z self) {
        self.equipment.chestplate
    }


    public static Z setBoots(Z self, ItemStack item) {
        self.equipment.boots = item
        self
    }

    public static Z setHelmet(Z self, ItemStack item) {
        self.equipment.helmet = item
        self
    }

    public static Z setLegs(Z self, ItemStack item) {
        self.equipment.leggings = item
        self
    }

    public static Z setHand(Z self, ItemStack item) {
        self.equipment.itemInHand = item
        self
    }

    public static Z setArmor(Z self, ItemStack item) {
        self.equipment.chestplate = item
        self
    }

    public static Z setBoots(Z self, Material item, int damage=0) {
        self.equipment.boots = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setHelmet(Z self, Material item, int damage=0) {
        self.equipment.helmet = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setLegs(Z self, Material item, int damage=0) {
        self.equipment.leggings = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setHand(Z self, Material item, int damage=0) {
        self.equipment.itemInHand = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setArmor(Z self, Material item, int damage=0) {
        self.equipment.chestplate = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setBoots(Z self, int item, int damage=0) {
        self.equipment.boots = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setHelmet(Z self, int item, int damage=0) {
        self.equipment.helmet = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setLegs(Z self, int item, int damage=0) {
        self.equipment.leggings = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setHand(Z self, int item, int damage=0) {
        self.equipment.itemInHand = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setArmor(Z self, int item, int damage=0) {
        self.equipment.chestplate = new ItemStack(item, 1, damage as short)
        self
    }

    public static Z setBoots(Z self, String item, int damage=0) {
        self.equipment.boots = new ItemStack(BukkitExtUtils.parseEnum(Material, item),1,damage as short)
        self
    }

    public static Z setHelmet(Z self, String item, int damage=0) {
        self.equipment.helmet = new ItemStack(BukkitExtUtils.parseEnum(Material, item),1,damage as short)
        self
    }

    public static Z setLegs(Z self, String item, int damage=0) {
        self.equipment.leggings = new ItemStack(BukkitExtUtils.parseEnum(Material, item),1,damage as short)
        self
    }

    public static Z setHand(Z self, String item, int damage=0) {
        self.equipment.itemInHand = new ItemStack(BukkitExtUtils.parseEnum(Material, item),1,damage as short)
        self
    }

    public static Z setArmor(Z self, String item, int damage=0) {
        self.equipment.chestplate = new ItemStack(BukkitExtUtils.parseEnum(Material, item),1,damage as short)
        self
    }


}
