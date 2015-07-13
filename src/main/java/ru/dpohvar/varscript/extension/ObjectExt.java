package ru.dpohvar.varscript.extension;

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import static groovy.lang.Closure.*;

public class ObjectExt {

    public static <T> T withIt(T self, @ClosureParams(FirstParam.FirstGenericType.class) Closure closure) {
        closure.setDelegate(self);
        closure.setResolveStrategy(DELEGATE_FIRST);
        closure.call(self);
        return self;
    }

}
