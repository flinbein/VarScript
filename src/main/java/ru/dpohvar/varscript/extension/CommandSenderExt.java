package ru.dpohvar.varscript.extension;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.dpohvar.varscript.workspace.CallerScript;

public class CommandSenderExt {

    public static <T extends CommandSender> T leftShift(T self, CharSequence str) {
        self.sendMessage(str.toString());
        return self;
    }



}
