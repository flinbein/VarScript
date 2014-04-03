package ru.dpohvar.varscript;

import org.bukkit.command.CommandSender;
import ru.dpohvar.varscript.utils.YamlUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manager of loaded workspaces
 */
public class WorkspaceManager {

    private File home;
    private File configFile;

    private Map<String, Workspace> workspaces = new HashMap<>();

    WorkspaceManager(VarScriptPlugin plugin) {
        this.home = new File(plugin.getDataFolder(), "workspace");
        if (!home.isDirectory() && !home.mkdirs()) {
            throw new RuntimeException("can not create directory " + home);
        }
        this.configFile = new File(home, "default.yml");
    }

    /**
     * checks whether workspace is loaded
     *
     * @param name name of workspace
     * @return true if workspace is successfully loaded
     */
    public boolean isWorkspaceLoaded(String name) {
        Workspace ws = workspaces.get(name);
        return ws != null && ws.getStatus() == Workspace.LOADED;
    }

    /**
     * get list of loaded workspaces
     *
     * @return list of names
     */
    public Set<String> getWorkspaces() {
        return new TreeSet<>(workspaces.keySet());
    }

    /**
     * get  or load workspace
     *
     * @param name name of workspace
     * @return loaded workspace or null on error
     */
    public Workspace getWorkspace(String name) {
        Workspace workspace = workspaces.get(name);
        if (workspace == null) {
            Map<String, Workspace> session = new HashMap<>();
            workspace = new Workspace(this, name, session);
            session.put(name, workspace);
            workspace.load();
        }
        if (workspace.getStatus() == Workspace.LOADED) {
            workspaces.put(name, workspace);
            return workspace;
        } else return null;
    }

    Workspace loadWorkspace(String name, Map<String, Workspace> session) {
        if (workspaces.containsKey(name)) return workspaces.get(name);
        Workspace workspace = session.get(name);
        if (workspace == null) {
            session.put(name, workspace = new Workspace(this, name, session));
        }
        if (workspace.getStatus() == Workspace.CREATED) {
            workspace.load();
        }
        if (workspace.getStatus() == Workspace.LOADED) return workspace;
        else return null;
    }

    /**
     * unload workspace
     *
     * @param name name of workspace
     */
    public void unloadWorkspace(String name) {
        Workspace workspace = workspaces.remove(name);
        if (workspace == null) return;
        try {
            workspace.unload();
        } catch (Exception e) {
            if (VarScriptPlugin.plugin.isDebug()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * load all available workspaces
     */
    public void loadAllWorkspaces() {
        loadWorkspaces(getReadyWorkspaces());
    }

    /**
     * load workspaces
     *
     * @param listToLoad list of workspaces that will be loaded
     */
    public void loadWorkspaces(List<String> listToLoad) {
        if (listToLoad == null) return;
        listToLoad.removeAll(workspaces.keySet());
        Map<String, Workspace> session = new HashMap<>();
        for (String wsName : listToLoad) {
            session.put(wsName, new Workspace(this, wsName, session));
        }
        for (Workspace ws : new ArrayList<>(session.values())) {
            if (ws.getStatus() == Workspace.CREATED) {
                ws.load();
            }
        }
        for (Workspace ws : session.values()) {
            if (ws.getStatus() == Workspace.LOADED) {
                workspaces.put(ws.getName(), ws);
            }
        }
    }

    /**
     * get workspaces in directory /workspaces
     *
     * @return list of names
     */
    public List<String> getReadyWorkspaces() {
        List<String> result = new ArrayList<>();
        File[] files = home.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) result.add(f.getName());
        }
        return result;
    }

    /**
     * unload all workspaces
     */
    public void unloadAllWorkspaces() {
        for (String name : this.getWorkspaces()) {
            if (workspaces.containsKey(name)) unloadWorkspace(name);
        }
    }


    /**
     * get name of workspace associated with script caller ("me")
     *
     * @param object script caller
     * @return name of associated workspace
     */
    public String getWorkspaceName(Object object) {
        if (object instanceof CommandSender) {
            return getWorkspaceName(((CommandSender) object).getName());
        }
        return getDefaultWorkspaceName();
    }

    private Map<String, String> commandSenderWorkspaces = new HashMap<>();

    /**
     * get current name of workspace associated with script caller
     *
     * @param senderName name of caller
     * @return associated workspace name
     */
    public String getWorkspaceName(String senderName) {
        String wsName = commandSenderWorkspaces.get(senderName);
        if (wsName != null) return wsName;
        wsName = getDefaultWorkspaceName(senderName);
        commandSenderWorkspaces.put(senderName, wsName);
        return wsName;
    }

    /**
     * set current workspace name to script caller
     *
     * @param senderName name of caller
     * @param wsName     name of workspace
     */
    public void setWorkspaceName(String senderName, String wsName) {
        commandSenderWorkspaces.put(senderName, wsName);
    }

    /**
     * get name of workspace associated with script caller by default
     *
     * @param senderName name of caller
     * @return default workspace name
     */
    public String getDefaultWorkspaceName(String senderName) {
        Object playersData = getConfigOption("sender");
        if (playersData instanceof Map) {
            Object wsName = ((Map) playersData).get(senderName);
            if (wsName instanceof String) return (String) wsName;
        }
        return senderName;
    }

    /**
     * set default workspace name to script caller
     *
     * @param senderName name of caller
     * @param wsName     name of workspace
     */
    public boolean setDefaultWorkspaceName(String senderName, String wsName) {
        try {
            Map config;
            if (!configFile.isFile()) {
                configFile.createNewFile();
                config = new HashMap();
            } else {
                config = (Map) YamlUtils.loadYaml(configFile);
            }
            if (config == null) config = new HashMap();
            Map playerMap = (Map) config.get("sender");
            if (playerMap == null) config.put("sender", playerMap = new HashMap());
            playerMap.put(senderName, wsName);
            return YamlUtils.dumpYaml(configFile, config);
        } catch (IOException e) {
            if (VarScriptPlugin.plugin.isDebug()) e.printStackTrace();
            return false;
        }
    }

    String defaultWorkspaceName;

    /**
     * get default workspace name not associated to caller
     *
     * @return name of workspace
     */
    public String getDefaultWorkspaceName() {
        if (defaultWorkspaceName != null) return defaultWorkspaceName;
        Object wsName = getConfigOption("default");
        if (!(wsName instanceof String)) wsName = "default";
        defaultWorkspaceName = (String) wsName;
        return defaultWorkspaceName;
    }

    private Object getConfigOption(String option) {
        if (configFile.isFile()) {
            Object config = YamlUtils.loadYaml(configFile);
            if (config instanceof Map) {
                return ((Map) config).get(option);
            }
        }
        return null;
    }
}



