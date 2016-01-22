package ru.dpohvar.varscript.extension;

import groovy.lang.Closure;
import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.api.*;
import me.dpohvar.powernbt.nbt.NBTBase;
import me.dpohvar.powernbt.utils.NBTUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import ru.dpohvar.varscript.extension.nbt.NBTCompoundProperties;
import ru.dpohvar.varscript.workspace.CallerScript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class PowerNBTExt {

    private static NBTManager nbtManager = NBTManager.getInstance();

    // entity

    public static NBTCompound getNbt(Entity self){
        return nbtManager.read(self);
    }

    public static void setNbt(Entity self, NBTCompound data){
       nbtManager.write(self, data);
    }

    public static void setNbt(Entity self, Map data){
       nbtManager.write(self, new NBTCompound(data));
    }

    public static void nbt(Entity self, Map data){
        NBTCompound tag = getNbt(self);
        if (tag == null) tag = new NBTCompound();
        tag.merge(data);
        setNbt(self, tag);
    }

    public static void leftShift(Entity self, Map data){
        nbt(self, data);
    }

    public static Object nbt(Entity self, Closure closure){
        NBTCompound tag = getNbt(self);
        NBTCompound ext = tag != null ? tag.clone() : new NBTCompound();
        closure.setDelegate(new NBTCompoundProperties(ext));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setResolveStrategy(1);
        Object result = closure.call(self);
        if (!ext.equals(tag)) setNbt(self, ext);
        return result;
    }

    // block

    public static NBTCompound getNbt(Block self){
        return nbtManager.read(self);
    }

    public static void setNbt(Block self, NBTCompound data){
       nbtManager.write(self, data);
    }

    public static void setNbt(Block self, Map data){
       nbtManager.write(self, new NBTCompound(data));
    }

    public static void nbt(Block self, Map data){
        NBTCompound tag = getNbt(self);
        if (tag == null) tag = new NBTCompound();
        tag.merge(data);
        setNbt(self, tag);
    }

    public static void leftShift(Block self, Map data){
        nbt(self, data);
    }

    public static Object nbt(Block selfBlock, Closure closure){
        NBTCompound tag = getNbt(selfBlock);
        NBTCompound ext = tag != null ? tag.clone() : new NBTCompound();
        closure.setDelegate(new NBTCompoundProperties(ext));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object result = closure.call(selfBlock);
        if (!ext.equals(tag)) setNbt(selfBlock, ext);
        return result;
    }

    // itemstack

    public static NBTCompound getNbt(ItemStack self){
        return nbtManager.read(self);
    }

    public static void setNbt(ItemStack self, NBTCompound data){
       nbtManager.write(self, data);
    }

    public static void setNbt(ItemStack self, Map data){
       nbtManager.write(self, new NBTCompound(data));
    }

    public static void nbt(ItemStack self, Map data){
        NBTCompound tag = getNbt(self);
        if (tag == null) tag = new NBTCompound();
        tag.merge(data);
        setNbt(self, tag);
    }

    public static void leftShift(ItemStack self, Map data){
        nbt(self, data);
    }

    public static Object nbt(ItemStack selfItemStack, Closure closure){
        NBTCompound tag = getNbt(selfItemStack);
        NBTCompound ext = tag != null ? tag.clone() : new NBTCompound();
        closure.setDelegate(new NBTCompoundProperties(ext));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object result = closure.call(selfItemStack);
        if (!ext.equals(tag)) setNbt(selfItemStack, ext);
        return result;
    }

    // chunk

    public static NBTCompound getNbt(Chunk self){
        return nbtManager.read(self);
    }

    public static void setNbt(Chunk self, NBTCompound data){
       nbtManager.write(self, data);
    }

    public static void setNbt(Chunk self, Map data){
       nbtManager.write(self, new NBTCompound(data));
    }

    public static void nbt(Chunk self, Map data){
        NBTCompound tag = getNbt(self);
        if (tag == null) tag = new NBTCompound();
        tag.merge(data);
        setNbt(self, tag);
    }

    public static void leftShift(Chunk self, Map data){
        nbt(self, data);
    }

    public static Object nbt(Chunk selfChunk, Closure closure){
        NBTCompound tag = getNbt(selfChunk);
        NBTCompound ext = tag != null ? tag.clone() : new NBTCompound();
        closure.setDelegate(new NBTCompoundProperties(ext));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object result = closure.call(selfChunk);
        if (!ext.equals(tag)) setNbt(selfChunk, ext);
        return result;
    }

    // file

    public static Object getNbt(File self) throws IOException {
        return nbtManager.read(self);
    }

    public static void setNbt(File self, Object data) throws IOException {
       nbtManager.write(self, data);
    }

    public static Object nbt(File self, Closure closure) throws IOException {
        Object tag = getNbt(self);

        if (tag != null) {
            if (tag instanceof NBTCompound) {
                closure.setDelegate(new NBTCompoundProperties((NBTCompound)tag));
            } else {
                closure.setDelegate(tag);
            }
        }
        Object result = closure.call(self);
        setNbt(self, tag);
        return result;
    }

    public static NBTCompound getCompressedNbt(File self) throws IOException {
        return nbtManager.readCompressed(self);
    }

    public static void setCompressedNbt(File self, NBTCompound data) throws IOException {
       nbtManager.writeCompressed(self, data);
    }

    public static void setCompressedNbt(File self, Map data) throws FileNotFoundException {
       nbtManager.writeCompressed(self, new NBTCompound(data));
    }

    public static Object compressedNbt(File self, Closure closure) throws IOException {
        NBTCompound tag = getCompressedNbt(self);
        NBTCompound ext = tag != null ? tag.clone() : new NBTCompound();
        closure.setDelegate(new NBTCompoundProperties(ext));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object result = closure.call(self);
        if (!ext.equals(tag)) setCompressedNbt(self, ext);
        return result;
    }

    // forge data

    public static NBTCompound getForgeData(Entity self) {
        return nbtManager.readForgeData(self);
    }

    public static void setForgeData(Entity self, NBTCompound data){
       nbtManager.write(self, data);
    }

    public static void setForgeData(Entity self, Map data){
       nbtManager.write(self, new NBTCompound(data));
    }

    public static void forgeData(Entity self, Map data){
        NBTCompound tag = getForgeData(self);
        if (tag == null) tag = new NBTCompound();
        tag.merge(data);
        setForgeData(self, tag);
    }

    public static Object forgeData(Entity self, Closure closure){
        NBTCompound tag = getForgeData(self);
        NBTCompound ext = tag != null ? tag.clone() : new NBTCompound();
        closure.setDelegate(new NBTCompoundProperties(ext));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object result = closure.call(self);
        if (!ext.equals(tag)) setForgeData(self, ext);
        return result;
    }

    public static Object getNbt(String self) throws IOException {
        return nbtManager.parseMojangson(self);
    }

    // offline nbt

    public static NBTCompound getOfflineNbt(OfflinePlayer self) throws IOException {
        return nbtManager.readOfflinePlayer(self);
    }

    public static void setOfflineNbt(OfflinePlayer self, NBTCompound data){
       nbtManager.writeOfflinePlayer(self, data);
    }

    public static void setOfflineNbt(OfflinePlayer self, Map data){
       nbtManager.writeOfflinePlayer(self, new NBTCompound(data));
    }

    public static void offlineNbt(OfflinePlayer self, Map data) throws IOException {
        NBTCompound tag = getOfflineNbt(self);
        if (tag == null) tag = new NBTCompound();
        tag.merge(data);
        setOfflineNbt(self, tag);
    }

    public static Object offlineNbt(OfflinePlayer self, Closure closure) throws IOException {
        NBTCompound tag = getOfflineNbt(self);
        NBTCompound ext = tag != null ? tag.clone() : new NBTCompound();
        closure.setDelegate(new NBTCompoundProperties(ext));
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        Object result = closure.call(self);
        if (!ext.equals(tag)) setOfflineNbt(self, ext);
        return result;
    }

    public static Object getNbtClipboard(CommandSender self) throws IOException {
        NBTBase nbt = PowerNBT.plugin.getCaller(self).getTag();
        return nbt == null ? null : NBTUtils.nbtUtils.getValue(nbt.getHandle());
    }

    public static void setNbtClipboard(CommandSender self, Object value) throws IOException {
        Object tag = NBTUtils.nbtUtils.createTag(value);
        NBTBase nbt = NBTBase.wrap(tag);
        PowerNBT.plugin.getCaller(self).setTag(nbt);
    }

    // caller script

    public static NBTCompound nbt(CallerScript self, Entity container) {
        return getNbt(container);
    }

    public static NBTCompound nbt(CallerScript self, Block container) {
        return getNbt(container);
    }

    public static NBTCompound nbt(CallerScript self, ItemStack container) {
        return getNbt(container);
    }

    public static NBTCompound nbt(CallerScript self, Chunk container) {
        return getNbt(container);
    }

    public static Object nbt(CallerScript self, File container) throws IOException {
        return getNbt(container);
    }

    public static NBTCompound compressedNbt(CallerScript self, File container) throws IOException {
        return getCompressedNbt(container);
    }

    public static NBTCompound forgeData(CallerScript self, Entity container) {
        return getForgeData(container);
    }

    public static NBTCompound offlineNbt(CallerScript self, OfflinePlayer container) throws IOException {
        return getOfflineNbt(container);
    }


    public static <T extends Entity> T nbt(CallerScript self, T container, Map data) {
        if (data instanceof NBTCompound) setNbt(container, (NBTCompound)data);
        else setNbt(container, data);
        return container;
    }

    public static Block nbt(CallerScript self, Block container, Map data) {
        if (data instanceof NBTCompound) setNbt(container, (NBTCompound)data);
        else setNbt(container, data);
        return container;
    }

    public static ItemStack nbt(CallerScript self, ItemStack container, Map data) {
        if (data instanceof NBTCompound) setNbt(container, (NBTCompound)data);
        else setNbt(container, data);
        return container;
    }

    public static Chunk nbt(CallerScript self, Chunk container, Map data) {
        if (data instanceof NBTCompound) setNbt(container, (NBTCompound)data);
        else setNbt(container, data);
        return container;
    }

    public static File nbt(CallerScript self, File container, Object data) throws IOException {
        setNbt(container, data);
        return container;
    }

    public static File compressedNbt(CallerScript self, File container, Map data) throws IOException {
        if (data instanceof NBTCompound) setCompressedNbt(container, (NBTCompound) data);
        else setCompressedNbt(container, data);
        return container;
    }

    public static <T extends Entity> T forgeData(CallerScript self, T container, Map data) {
        if (data instanceof NBTCompound) setForgeData(container, (NBTCompound) data);
        else setForgeData(container, data);
        return container;
    }

    public static OfflinePlayer offlineNbt(CallerScript self, OfflinePlayer container, Map data) throws IOException {
        if (data instanceof NBTCompound) setOfflineNbt(container, (NBTCompound) data);
        else setOfflineNbt(container, data);
        return container;
    }


    public static Object nbt(CallerScript self, Entity container, Closure closure) {
        return nbt(container, closure);
    }

    public static Object nbt(CallerScript self, Block container, Closure closure) {
        return nbt(container, closure);
    }

    public static Object nbt(CallerScript self, ItemStack container, Closure closure) {
        return nbt(container, closure);
    }

    public static Object nbt(CallerScript self, Chunk container, Closure closure) {
        return nbt(container, closure);
    }

    public static Object nbt(CallerScript self, File container, Closure closure) throws IOException {
        return nbt(container, closure);
    }

    public static Object compressedNbt(CallerScript self, File container, Closure closure) throws IOException {
        return compressedNbt(container, closure);
    }

    public static Object forgeData(CallerScript self, Entity container, Closure closure) {
        return forgeData(container, closure);
    }

    public static Object offlineNbt(CallerScript self, OfflinePlayer container, Closure closure) throws IOException {
        return offlineNbt(container, closure);
    }

    public static Object nbt(CallerScript self, String value) throws IOException {
        return getNbt(value);
    }

    public static Object nbt(CallerScript self, Collection value) throws IOException {
        return new NBTList(value);
    }

    public static Object nbt(CallerScript self, Map value) throws IOException {
        return new NBTCompound(value);
    }

    public static Object nbt(CallerScript self, Object[] value) throws IOException {
        return new NBTList(value);
    }

    public static Object getNbtClipboard(CallerScript self) throws IOException {
        return getNbtClipboard(self.getCaller().getSender());
    }

    public static void setNbtClipboard(CallerScript self, Object value) throws IOException {
        setNbtClipboard(self.getCaller().getSender(), value);
    }

}


























