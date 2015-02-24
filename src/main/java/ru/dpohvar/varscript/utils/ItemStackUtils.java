package ru.dpohvar.varscript.utils;
import org.bukkit.inventory.ItemStack;
import static ru.dpohvar.varscript.utils.ReflectionUtils.*;

public final class ItemStackUtils {

    public static final ItemStackUtils itemStackUtils = new ItemStackUtils();
    private ReflectionUtils.RefClass classCraftItemStack = getRefClass("{cb}.inventory.CraftItemStack, {CraftItemStack}");
    private ReflectionUtils.RefClass classItemStack = getRefClass("{nms}.ItemStack, {nm}.item.ItemStack, {ItemStack}");

    private RefMethod asNMSCopy;
    private RefMethod asCraftMirror;
    private RefConstructor conNmsItemStack;
    private RefConstructor conCraftItemStack;

    private ItemStackUtils(){
        try {
            asNMSCopy = classCraftItemStack.findMethod(
                    new MethodCondition()
                            .withTypes(ItemStack.class)
                            .withReturnType(classItemStack)
            );
            asCraftMirror = classCraftItemStack.findMethod(
                    new MethodCondition()
                            .withTypes(classItemStack)
                            .withReturnType(classCraftItemStack)
            );
        } catch (Exception e) {
            conNmsItemStack = classItemStack.getConstructor(int.class, int.class, int.class);
            conCraftItemStack = classCraftItemStack.getConstructor(classItemStack);
        }

    }

    @SuppressWarnings("deprecation")
    public Object createNmsItemStack(ItemStack itemStack){
        if (asNMSCopy != null) {
            return asNMSCopy.call(itemStack);
        } else {
            int type = itemStack.getTypeId();
            int amount = itemStack.getAmount();
            int data = itemStack.getData().getData();
            return conNmsItemStack.create(type, amount, data);
        }
    }

    public ItemStack createCraftItemStack(Object nmsItemStack){
        if (asCraftMirror != null) {
            return (ItemStack) asCraftMirror.call(nmsItemStack);
        } else {
            return (ItemStack) conCraftItemStack.create(nmsItemStack);
        }
    }

    public ItemStack createCraftItemStack(ItemStack item){
        return createCraftItemStack(createNmsItemStack(item));
    }

}