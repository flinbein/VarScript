package ru.dpohvar.varscript.trigger;

import org.bukkit.event.Listener;
import ru.dpohvar.varscript.workspace.Workspace;

public interface Trigger extends Listener {

    Workspace getWorkspace();

    boolean isStopped();

    boolean stop();
}
