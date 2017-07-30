package ru.dpohvar.varscript.command.git;

import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.scheduler.BukkitScheduler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.caller.Caller;

import java.util.Date;

import static org.bukkit.conversations.Conversation.ConversationState.ABANDONED;

public class GitExecutor<T> extends CredentialsProvider implements Runnable, ProgressMonitor, Prompt {
    private static final String cancelPattern = "/?cancel|/?stop";

    private final Caller caller;
    private final String callerWorkspaceName;
    private final BukkitScheduler scheduler;
    private final VarScript plugin;
    private final ConversationFactory conversationFactory;
    private final Object sync = new Object();
    private final GitResultHandler<T> handler;

    private GitCommand<T> command;
    private String lastInputMessage;
    private boolean awaitInput = false;
    private boolean finished = false;
    private Conversation conversation;

    public GitExecutor(Caller caller, String callerWorkspaceName, GitResultHandler<T> handler) {
        this.caller = caller;
        this.callerWorkspaceName = callerWorkspaceName;
        this.plugin = caller.getService().getPlugin();
        this.handler = handler;
        this.scheduler = plugin.getServer().getScheduler();
        this.conversationFactory = new ConversationFactory(plugin)
                .withLocalEcho(false)
                .withModality(true)
                .withFirstPrompt(this);
    }

    public void setCommand(GitCommand<T> command) {
        this.command = command;
    }

    @Override
    public void run() {
        T result = null;
        if (caller.getSender() instanceof Conversable) {
            conversation = conversationFactory.buildConversation(((Conversable) caller.getSender()));
            conversation.begin();
        }
        try {
            result = command.call();
            finished = true;
            if (conversation != null && !conversation.getState().equals(ABANDONED)){
                conversation.abandon();
            }
            if (handler != null) handler.handle(command, caller, callerWorkspaceName, result);
        } catch (Throwable e) {
            finished = true;
            scheduler.runTaskLater(plugin,new MessageSender(caller,e,callerWorkspaceName),1);
        } finally {
            try {
                Repository repository = command.getRepository();
                if (repository != null) repository.close();
                if (result instanceof Git) {
                    repository = ((Git) result).getRepository();
                    repository.close();
                }
            } catch (Throwable e) {
                scheduler.runTaskLater(plugin,new MessageSender(caller,e,callerWorkspaceName),1);
            }
            if (conversation != null && !conversation.getState().equals(ABANDONED)){
                conversation.abandon();
            }
        }

    }

    private int size = 0;
    private String job = "";
    private long lastSendMessageTime = 0;

    @Override
    public boolean isCancelled() {
        return finished;
    }

    @Override
    public void endTask() {
    }

    @Override
    public void update(int progress) {
        if (finished) return;
        long time = new Date().getTime();
        if (time > lastSendMessageTime + 1000) return;
        lastSendMessageTime = time;
        int percent = progress*100/size;
        String message = ChatColor.AQUA + job + ChatColor.GRAY+" [" + percent + "%]";
        scheduler.runTask(plugin,new MessageSender(caller, message, callerWorkspaceName,0));
    }

    @Override
    public void beginTask(String job, int size) {
        String message = "starting "+ChatColor.AQUA + job;
        scheduler.runTask(plugin,new MessageSender(caller, message, callerWorkspaceName,0));
        this.job = job;
        if (size < 1) size = 1;
        this.size = size;
    }

    @Override
    public void start(int i) {
    }


    @Override
    public String getPromptText(ConversationContext conversationContext) {
        return "";
    }

    @Override
    public boolean blocksForInput(ConversationContext conversationContext) {
        return true;
    }

    @Override
    public Prompt acceptInput(ConversationContext conversationContext, String msg) {
        synchronized (sync) {
            lastInputMessage = msg;
            if (!awaitInput && msg.matches(cancelPattern)) {
                finished = true;
            }
            sync.notifyAll();
        }
        return this;
    }

    @Override
    public boolean isInteractive() {
        return conversation != null;
    }

    @Override
    public boolean supports(CredentialItem... credentialItems) {
        return conversation != null;
    }

    @Override
    public boolean get(URIish urIish, CredentialItem... credentialItems) throws UnsupportedCredentialItem {
        if (conversation == null) return false;
        for (CredentialItem credentialItem : credentialItems) {
            String message = ChatColor.GOLD + ">>> " + ChatColor.RESET + credentialItem.getPromptText();
            if (credentialItem instanceof CredentialItem.InformationalMessage){
                scheduler.runTask(plugin,new MessageSender(caller, message, callerWorkspaceName,0));
                continue;
            }
            boolean wrongInput = false;
            do {
                String inputMessage;
                synchronized (sync) {
                    lastInputMessage = null;
                    scheduler.runTask(plugin, new MessageSender(caller, message, callerWorkspaceName, 2));
                    awaitInput = true;
                    while (!finished && lastInputMessage == null) {
                        try {
                            sync.wait(1000);
                            if (conversation.getState().equals(ABANDONED)) {
                                finished = true;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    awaitInput = false;
                    if (finished) return false;
                    inputMessage = lastInputMessage;
                }
                if (credentialItem instanceof CredentialItem.YesNoType) {
                    if (inputMessage.matches("yes|true|1|on|y|\\+|valid|correct|right")) {
                        ((CredentialItem.YesNoType) credentialItem).setValue(true);
                    } else if (inputMessage.matches("no|false|n|0|-|invalid|incorrect|wrong")) {
                        ((CredentialItem.YesNoType) credentialItem).setValue(false);
                    } else {
                        wrongInput = true;
                    }
                } else if (credentialItem instanceof CredentialItem.StringType) {
                    ((CredentialItem.StringType) credentialItem).setValue(inputMessage);

                } else if (credentialItem instanceof CredentialItem.CharArrayType) {
                    ((CredentialItem.CharArrayType) credentialItem).setValue(inputMessage.toCharArray());
                }
            } while (wrongInput);
        }
        return true;
    }
}
