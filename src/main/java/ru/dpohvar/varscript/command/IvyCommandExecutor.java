package ru.dpohvar.varscript.command;

import org.apache.commons.lang.StringUtils;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.boot.BootHelper;
import ru.dpohvar.varscript.caller.Caller;

import java.io.IOException;
import java.text.ParseException;

public class IvyCommandExecutor implements CommandExecutor{

    private final VarScript plugin;

    public IvyCommandExecutor(VarScript plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if ( 1 > strings.length || strings.length > 2 ) return false;
        Caller caller = plugin.getCallerService().getCaller(commandSender);
        ModuleRevisionId mrId = ModuleRevisionId.parse(strings[0]);
        String conf;
        if (strings.length == 2) conf = strings[1];
        else conf = "default";
        try {
            caller.sendMessage("resolving "+mrId);
            ResolveReport resolveReport = BootHelper.resolveIvy(mrId, conf);
            if (resolveReport.hasError()) {
                caller.sendErrorMessage(StringUtils.join(resolveReport.getProblemMessages(),'\n'));
                return true;
            }
            caller.sendMessage("loading dependencies of "+mrId);
            BootHelper.loadReportedArtifacts(resolveReport, plugin.getName());
            caller.sendMessage(mrId + " loaded");
        } catch (Exception e) {
            caller.sendThrowable(e);
        }
        return true;


    }
}
