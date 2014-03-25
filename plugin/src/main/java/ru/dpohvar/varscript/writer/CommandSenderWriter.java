package ru.dpohvar.varscript.writer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import ru.dpohvar.varscript.VarScriptPlugin;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

/**
 * Created by DPOH-VAR on 02.03.14
 */
public class CommandSenderWriter extends Writer {

    static final BukkitScheduler scheduler = Bukkit.getScheduler();
    final CommandSender sender;
    final String prefix;
    StringBuilder builder = new StringBuilder();
    BukkitTask task = null;

    public CommandSenderWriter(CommandSender sender, String prefix) {
        this.prefix = prefix;
        this.sender = sender;
    }

    @Override
    public synchronized void  write(char[] buf, int off, int len) throws IOException {
        builder.append(buf, off, len);
        if (task == null) {
            task = scheduler.runTask(VarScriptPlugin.plugin, new SendBufferTask(this));
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}
