package ru.dpohvar.varscript.boot;
import groovy.lang.GroovyClassLoader;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import ru.dpohvar.varscript.utils.ReflectionUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class VarScriptClassLoader extends URLClassLoader {

    private PluginManager pluginManager;

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    public Package[] getPackages() {
        return super.getPackages();
    }

    public VarScriptClassLoader(ClassLoader parent, PluginManager pluginManager) {
        super(new URL[]{}, parent);
        this.pluginManager = pluginManager;
    }

    private Map<String,Class<?>> cacheClasses = new HashMap<>();

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        checkFolderUpdates();

        // load from cache
        var result = cacheClasses.get(name);
        if (result != null) return result;

        // try load from parent, cache
        try {
            result = getParent().loadClass(name);
            if (result != null) {
                cacheClasses.put(name, result);
                return result;
            }
        } catch (ClassNotFoundException ignored){}
        String modifiedName = getModifiedClassName(name);
        if (modifiedName != null && !modifiedName.equals(name)) try {
            result = getParent().loadClass(modifiedName);
            if (result != null) {
                cacheClasses.put(name, result);
                cacheClasses.put(modifiedName, result);
                return result;
            }
        } catch (ClassNotFoundException ignored){}

        // load class in plugins
        for (Plugin plugin : pluginManager.getPlugins()) {
            var pluginClassLoader = plugin.getClass().getClassLoader();
            try {
                result = pluginClassLoader.loadClass(name);
                return result;
            } catch (ClassNotFoundException ignored) {}
        }

        result = super.findClass(name);
        if (result != null) return result;
        throw new ClassNotFoundException(name);
    }

    private Class<?> tryLoadAndCacheClass(ClassLoader loader, String name){
        try {
            var result = loader.loadClass(name);
            cacheClasses.put(name, result);
            return result;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private final String cbVersionSuffix = ReflectionUtils.getCbVersionSuffix();
    private final String nmsVersionSuffix = ReflectionUtils.getNmsVersionSuffix();

    private String getModifiedClassName(String className){
        boolean useCb = className.startsWith( "org.bukkit.craftbukkit");
        boolean useNms = className.startsWith("net.minecraft.server");
        if (!useCb && !useNms) return null;
        String[] sp = className.split("\\.");
        if (sp.length < 3) return null;
        String prefix = sp[0]+"."+sp[1]+"."+sp[2];
        String versionTag = null;
        if (sp.length > 3) versionTag = sp[3];
        if (versionTag != null && !versionTag.matches("v.+_.+")) versionTag = null;
        if (versionTag == null) {
            if (useCb && cbVersionSuffix == null) return null;
            if (useNms && nmsVersionSuffix == null) return null;
        } else {
            if (useCb && versionTag.equals(cbVersionSuffix)) return null;
            if (useNms && versionTag.equals(nmsVersionSuffix)) return null;
        }

        int endIndex = 4;
        if (versionTag == null) endIndex = 3;
        String postfix = "";
        for (int i= endIndex; i<sp.length; i++) postfix += "."+sp[i];

        if (useCb) {
            if (cbVersionSuffix == null) return prefix + postfix;
            else return prefix + "." + cbVersionSuffix + postfix;
        } else {
            if (nmsVersionSuffix == null) return prefix + postfix;
            else return prefix + "." + nmsVersionSuffix + postfix;
        }
    }

    public void monitorFolder(GroovyClassLoader groovyClassLoader, File serviceFolder){
        this.groovyClassLoader = groovyClassLoader;
        this.serviceFolder = serviceFolder;
    }

    private GroovyClassLoader groovyClassLoader;
    private File serviceFolder;
    private int lastClassLoaderUrlListLength = 0;

    private boolean checkFolderUpdates(){
        if (groovyClassLoader == null || serviceFolder == null) return false;
        File[] files = serviceFolder.listFiles();
        if (files != null) for (File file : files) {
            if (!file.isDirectory()) continue;
            try {
                groovyClassLoader.addURL(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        int urlListLength = groovyClassLoader.getURLs().length;
        if (lastClassLoaderUrlListLength == urlListLength) return false;
        lastClassLoaderUrlListLength = urlListLength;
        return true;
    }

}


















