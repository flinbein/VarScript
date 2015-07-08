package ru.dpohvar.varscript.utils;

import org.bukkit.Server;
import ru.dpohvar.varscript.caller.Caller;
import ru.dpohvar.varscript.workspace.Workspace;
import ru.dpohvar.varscript.workspace.WorkspaceService;

public interface ScriptProperties {

    public Caller getCaller();

    public Workspace getWorkspace();

    public Object getMe();

    public Object get_();

    public Server getServer();

    public WorkspaceService getGlobal();
}
