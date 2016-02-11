package ru.dpohvar.varscript.command.git;

import org.eclipse.jgit.api.GitCommand;
import ru.dpohvar.varscript.caller.Caller;


public interface GitResultHandler<T> {
    void handle(GitCommand<T> command, Caller caller, String callerWorkspaceName, T result);
}
