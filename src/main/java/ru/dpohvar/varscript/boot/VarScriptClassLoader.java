package ru.dpohvar.varscript.boot;

import groovy.lang.GroovyClassLoader;
import ru.dpohvar.varscript.utils.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class VarScriptClassLoader extends URLClassLoader {

    public static final boolean TO_PARENT = true;
    public static final boolean TO_SELF = false;

    static Method addURL, getURLs;
    static {
        try {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            getURLs = URLClassLoader.class.getDeclaredMethod("getURLs");
            getURLs.setAccessible(true);
        } catch (NoSuchMethodException ignored) {}
    }

    public VarScriptClassLoader(URLClassLoader parent) {
        super(new URL[]{}, parent);
    }

    public void addLibUrl(URL url, boolean parent){
        ClassLoader target = parent ? getParent() : this;
        try {
            URL[] urls = (URL[]) getURLs.invoke(target);
            for (URL t: urls) if (url.equals(t)) return;
            addURL.invoke(target, url);
        } catch (Exception e) {
            throw new RuntimeException("Error on add url to class loader: "+url,e);
        }
    }

    public void addLibUrl(URL url){
        addLibUrl(url, false);
    }

    public void addLibFile(File file, boolean parent){
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (Exception e) {
            throw new RuntimeException("Invalid file: "+file,e);
        }
        addLibUrl(url, parent);
    }
    public void addLibFile(File file){
        addLibFile(file, false);
    }


    private Map<String,Class<?>> classes = new HashMap<String,Class<?>>();

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (folderHasChanges()) return this.loadClass(name);
        Class result = classes.get(name);
        if (result != null) return result;
        String modifiedName = getModifiedClassName(name);
        if (modifiedName != null) try {
            result = getParent().loadClass(modifiedName);
            if (result != null) {
                classes.put(name, result);
                classes.put(modifiedName, result);
                return result;
            }
        } catch (ClassNotFoundException ignored){}

        result = super.findClass(name);
        if (result != null) return result;
        throw new ClassNotFoundException(name);
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
    private boolean folderHasChanges(){
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


















