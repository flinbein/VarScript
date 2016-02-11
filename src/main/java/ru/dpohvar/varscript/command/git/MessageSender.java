package ru.dpohvar.varscript.command.git;

import org.bukkit.scheduler.BukkitScheduler;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;

/**
 * Created by dpohv on 007 07 02 16.
 */
public class MessageSender implements Runnable {

    private final CharSequence message;
    private final Caller caller;
    private final String source;
    private final Throwable throwable;
    private final int type;

    public MessageSender(Caller caller, CharSequence message, String source, int type){
        this.message = message;
        this.caller = caller;
        this.source = source;
        this.type = type;
        throwable = null;
    }

    public MessageSender(Caller caller, Throwable throwable, String source){
        this.throwable = throwable;
        this.message = null;
        this.caller = caller;
        this.source = source;
        this.type = 2;
    }

    @Override
    public void run() {
        switch (type) {
            case 0:
                caller.sendMessage(message, source);
                break;
            case 1:
                caller.sendErrorMessage(message, source);
                break;
            case 2:
                if (throwable != null) caller.sendThrowable(throwable, source);
                else caller.sendPrintMessage(message, source);
                break;
        }
    }
}
