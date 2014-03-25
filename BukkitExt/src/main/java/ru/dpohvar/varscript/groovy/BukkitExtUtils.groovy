package ru.dpohvar.varscript.groovy

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import ru.dpohvar.varscript.VarScriptPlugin
import ru.dpohvar.varscript.Workspace
import static ru.dpohvar.varscript.utils.ReflectionUtils.*

/**
 * Created by DPOH-VAR on 07.03.14.
 */
@CompileStatic
class BukkitExtUtils {

    @CompileStatic(TypeCheckingMode.SKIP)
    public static<T> T parseEnum(Class<T> enumClass, String s) {
        try {
            return enumClass.valueOf(s)
        } catch (Throwable ignored) {
        }

        def temp = null
        s = s.toLowerCase()
        for (def example in enumClass.values()) {
            String e = example.toString().toLowerCase()
            if (e.equals(s)) return example
            if (temp == null && e.startsWith(s)) temp = example
        }
        return temp
    }

    static RefClass rcCraftItemStack = getRefClass "{cb}.inventory.CraftItemStack"
    static RefClass rcNMSItemStack = getRefClass "{nms}.ItemStack"
    static RefClass rcNMSItem = getRefClass "{nms}.Item"

    static RefMethod metNBTItem_d = rcNMSItem.getMethod "d", int

    public static ItemStack item(int id, int data=0, int amount=1){
        def nmsItem = metNBTItem_d.of(null).call(id)
        def nmsItemStack = rcNMSItemStack.realClass.newInstance(nmsItem, amount, data)
        rcCraftItemStack.realClass.newInstance(nmsItemStack) as ItemStack
    }

    public static ItemStack item(Material material, int data=0, int amount=1){
        item material.id, data, amount
    }

    public static ItemStack item(String material, int data=0, int amount=1){
        item parseEnum(Material,material).id, data, amount
    }

    public static List<Entity> getEntities() {
        List<Entity> result = []
        Bukkit.worlds.each { World it -> result.addAll it.entities }
        result
    }

    public static List<Entity> getMobs() {
        List<Entity> result = []
        Bukkit.worlds.each { World it -> result.addAll it.livingEntities }
        result.findAll { !(it instanceof Player) } as List
    }

    public static List<Entity> getLiving() {
        List<Entity> result = []
        Bukkit.worlds.each { World it -> result.addAll it.livingEntities }
        result
    }

    public static List<Entity> getItems() {
        List<Entity> result = []
        Bukkit.worlds.each {
            result.addAll it.findAll {it instanceof Item}
        }
        result
    }

    static class Teleporter{
        List<? extends Entity> entities
        Location caller
        public void to (Location loc) {
            entities.each {Entity it -> it.teleport loc}
        }
        public void to (Entity e) {
            entities.each {Entity it -> it.teleport e}
        }
        public void to (Block b) {
            entities.each {Entity it -> it.teleport b.location}
        }
        public void up (double y) {
            entities.each {Entity it -> it.teleport it.location.add(0,y,0) }
        }
        public void down (double y) {
            entities.each {Entity it -> it.teleport it.location.add(0,-y,0) }
        }
        public def getHere () {
            entities.each {Entity it -> it.teleport caller}
            null
        }
        public void forward (double len) {
            entities.each {Entity it -> it.teleport it.location.add(it.location.direction.multiply(len))}
        }
    }

    public static interface Trigger{
        public boolean stop()
        public boolean isStopped()
    }

    public static class EventTrigger implements Trigger{
        Workspace workspace
        long id
        private boolean stopped
        public boolean stop(){
            stopped = true
            workspace.stopEvent id
        }
        public boolean isStopped(){
            stopped
        }
    }

    public static class PeriodTrigger implements Trigger{
        Workspace workspace
        long id
        private boolean stopped
        public boolean stop(){
            stopped = true
            workspace.stopPeriod id
        }
        public boolean isStopped(){
            stopped
        }
    }

    public static class DelayTrigger implements Trigger{
        Workspace workspace
        long id
        private boolean stopped
        public boolean stop(){
            stopped = true
            workspace.stopDelay id
        }
        public boolean isStopped(){
            stopped
        }
    }

    public static class AsyncPeriodTrigger implements Trigger{
        Workspace workspace
        long id
        private boolean stopped
        public boolean stop(){
            stopped = true
            workspace.stopAsyncPeriod id
        }
        public boolean isStopped(){
            stopped
        }
    }

    public static class AsyncDelayTrigger implements Trigger{
        Workspace workspace
        long id
        private boolean stopped
        public boolean stop(){
            stopped = true
            workspace.stopAsyncDelay id
        }
        public boolean isStopped(){
            stopped
        }
    }

    public static class FinisherTrigger implements Trigger{
        Workspace workspace
        long id
        private boolean stopped
        public boolean stop(){
            stopped = true
            workspace.stopFinisher id
        }
        public boolean isStopped(){
            stopped
        }
    }

    private static final def eventClassPrefix = [
        "org.bukkit.event.",
        "org.bukkit.event.block.",
        "org.bukkit.event.enchantment.",
        "org.bukkit.event.entity.",
        "org.bukkit.event.hanging.",
        "org.bukkit.event.inventory.",
        "org.bukkit.event.painting.",
        "org.bukkit.event.player.",
        "org.bukkit.event.server.",
        "org.bukkit.event.vehicle.",
        "org.bukkit.event.weather.",
        "org.bukkit.event.world.",
        "me.dpohvar.varscript.event.",
    ]

    public static Class<? extends Event> getEventClass(String className) {
        Class<? extends Event> eventClass = null;
        try {
            return (Class<? extends Event>) VarScriptPlugin.plugin.libClassLoader.loadClass(className)
        } catch (Exception ignored) {
        }

        if (eventClass == null) for (String prefix : eventClassPrefix) try {
            return (Class<? extends Event>) VarScriptPlugin.plugin.libClassLoader.loadClass(prefix + className);
        } catch (Exception ignored) {
        }
        if (!className.endsWith("Event")) try {
            return getEventClass(className + "Event");
        } catch (Exception ignored){
        }
        throw new RuntimeException("event class "+className+" not found");
    }
}