package ru.dpohvar.varscript.groovy.nbt

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import static ru.dpohvar.varscript.utils.ReflectionUtils.*

/**
 * Created by DPOH-VAR on 06.03.14.
 */
@CompileStatic
class NBTUtils {

    @SuppressWarnings("GrUnresolvedAccess")
    public static RefClass rcNBTBase = getRefClass "{nms}.NBTBase", "net.minecraft.nbt.NBTBase"
    public static RefClass rcNBTNumber = getRefClass "{nms}.NBTNumber", "net.minecraft.nbt.NBTNumber"
    public static RefClass rcNBTTagByte = getRefClass "{nms}.NBTTagByte", "net.minecraft.nbt.NBTTagByte"
    public static RefClass rcNBTTagShort = getRefClass "{nms}.NBTTagShort", "net.minecraft.nbt.NBTTagShort"
    public static RefClass rcNBTTagInt = getRefClass "{nms}.NBTTagInt", "net.minecraft.nbt.NBTTagInt"
    public static RefClass rcNBTTagLong = getRefClass "{nms}.NBTTagLong", "net.minecraft.nbt.NBTTagLong"
    public static RefClass rcNBTTagFloat = getRefClass "{nms}.NBTTagFloat", "net.minecraft.nbt.NBTTagFloat"
    public static RefClass rcNBTTagDouble = getRefClass "{nms}.NBTTagDouble", "net.minecraft.nbt.NBTTagDouble"
    public static RefClass rcNBTTagByteArray = getRefClass "{nms}.NBTTagByteArray", "net.minecraft.nbt.NBTTagByteArray"
    public static RefClass rcNBTTagIntArray = getRefClass "{nms}.NBTTagIntArray", "net.minecraft.nbt.NBTTagIntArray"
    public static RefClass rcNBTTagString = getRefClass "{nms}.NBTTagString", "net.minecraft.nbt.NBTTagString"
    public static RefClass rcNBTTagCompound = getRefClass "{nms}.NBTTagCompound", "net.minecraft.nbt.NBTTagCompound"
    public static RefClass rcNBTTagList = getRefClass "{nms}.NBTTagList", "net.minecraft.nbt.NBTTagList"
    public static RefClass rcNBTCompressedStreamTools = getRefClass "{nms}.NBTCompressedStreamTools", "net.minecraft.nbt.NBTCompressedStreamTools"
    public static RefClass rcCraftItemStack = getRefClass "{cb}.inventory.CraftItemStack"
    
    public static RefConstructor conNBTTagByte = rcNBTTagByte.getConstructor byte
    public static RefConstructor conNBTTagShort = rcNBTTagShort.getConstructor short
    public static RefConstructor conNBTTagInt = rcNBTTagInt.getConstructor int
    public static RefConstructor conNBTTagLong = rcNBTTagLong.getConstructor long
    public static RefConstructor conNBTTagFloat = rcNBTTagFloat.getConstructor float
    public static RefConstructor conNBTTagDouble = rcNBTTagDouble.getConstructor double
    public static RefConstructor conNBTTagByteArray = rcNBTTagByteArray.getConstructor ((byte[]))
    public static RefConstructor conNBTTagString = rcNBTTagString.getConstructor String
    public static RefConstructor conNBTTagIntArray = rcNBTTagIntArray.getConstructor ((int[]))
    public static RefConstructor conNBTTagCompound = rcNBTTagCompound.getConstructor ()
    public static RefConstructor conNBTTagList = rcNBTTagList.getConstructor ()

    public static RefField fieldNBTTagCompoundMap = rcNBTTagCompound.findField Map
    public static RefField fieldNBTTagListList = rcNBTTagList.findField List

    public static RefMethod methodNBTTypeId = rcNBTBase.findMethodByReturnType byte

    @CompileStatic(TypeCheckingMode.SKIP)
    public static Object getValue(Object tag) {
        if (tag==null) return null;
        switch (tag.typeId as int){
            case 1..8: case 11: return tag.@data
            case 9: return NBTList.forNBT(tag)
            case 10: return NBTCompound.forNBT(tag)
            default: throw new RuntimeException("unexpected tag: "+tag.getClass())
        }
    }

    public static Object createTag(Object javaObject) {
        switch (javaObject) {
            case null: return null
            case Map: return new NBTCompound(javaObject as Map).getHandle()
            case Collection: return new NBTList(javaObject as Collection).getHandle()
            case Boolean: return conNBTTagByte.create((javaObject?1:0) as byte)
            case Byte: return conNBTTagByte.create(javaObject as byte)
            case Short: return conNBTTagShort.create(javaObject)
            case Integer: return conNBTTagInt.create(javaObject)
            case Long: return conNBTTagLong.create(javaObject)
            case Float: return conNBTTagFloat.create(javaObject)
            case Double: return conNBTTagDouble.create(javaObject)
            case String: return conNBTTagString.create(javaObject)
            case (byte[]): return conNBTTagByteArray.create(javaObject)
            case (int[]): return conNBTTagIntArray.create(javaObject)
            case (Object[]): return new NBTList(javaObject as List).getHandle()
            default: throw new RuntimeException("can not cast to nbt: $javaObject")
        }

    }

}
