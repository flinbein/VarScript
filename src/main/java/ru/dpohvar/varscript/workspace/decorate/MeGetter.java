package ru.dpohvar.varscript.workspace.decorate;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.VariableContainer;
import ru.dpohvar.varscript.workspace.VariableGetter;

public class MeGetter implements VariableGetter {

    @Override
    public Object getValue(String name, VariableContainer current, VariableContainer requester) {
        if (!name.equals("me")) return SKIP_GETTER;
        Caller caller = (Caller) requester.getHardVariables().get("caller");
        CommandSender sender = caller.getSender();
        if (sender instanceof BlockCommandSender) return ((BlockCommandSender) sender).getBlock();
        else return sender;
    }
}
