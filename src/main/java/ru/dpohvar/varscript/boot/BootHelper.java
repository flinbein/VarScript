package ru.dpohvar.varscript.boot;

import groovy.grape.Grape;
import groovy.grape.GrapeIvy;
import groovy.lang.GroovySystem;
import groovy.lang.MetaMethod;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.NumberConversions;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.m12n.ExtensionModuleScanner;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.yaml.snakeyaml.Yaml;
import ru.dpohvar.varscript.VarScript;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.text.ParseException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class BootHelper {

    private static Ivy ivy;

    private static Method addURL, getURLs;
    static {
        try {
            addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            getURLs = URLClassLoader.class.getDeclaredMethod("getURLs");
            getURLs.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error on init ClassLoaderInjector",e);
        }
    }

    /**
     * Download (if needed) and load ivy.jar,
     * create Ivy instance
     * and apply ivysettings.xml
     *
     * @return Ivy instance
     */
    public static Ivy prepareIvy(){
        ClassLoader resourceClassLoader = VarScript.class.getClassLoader();
        VarScriptClassLoader libLoader = VarScript.libLoader;

        if (ivy != null) return ivy;
        try {
            libLoader.loadClass("org.apache.ivy.Ivy");
        } catch (ClassNotFoundException ignored) {
            File ivyJarFile = new File(getConfigString("ivy.jar", null));
            if (!ivyJarFile.isFile()){
                String ivyDownloadURL = getConfigString("ivy.download-url", null);
                downloadIvyJar(ivyDownloadURL, ivyJarFile);
            }
            libLoader.addLibFile(ivyJarFile, VarScriptClassLoader.TO_PARENT);
        }

        ivy = new Ivy();
        IvySettings settings = new IvySettings();

        File ivySettingsXmlFile = new File(VarScript.dataFolder, "ivysettings.xml");
        try {
            if (ivySettingsXmlFile.isFile()) settings.load(ivySettingsXmlFile);
            else settings.load(resourceClassLoader.getResource("ivysettings.xml"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ivy.setSettings(settings);
        ivy.bind();

        return ivy;
    }


    private static YamlConfiguration localConfig; // on inner local config resource
    private static YamlConfiguration fileConfig; // on config file

    private static void loadConfigs(){
        if (localConfig == null) {
            // try to read local config resource
            InputStream resourceStream = null;
            try {
                resourceStream = VarScript.class.getClassLoader().getResourceAsStream("config.yml");
                if (resourceStream == null) throw new RuntimeException("Can not access to local resource config.yml");
                localConfig = new YamlConfiguration();
                localConfig.load(new InputStreamReader(resourceStream,"UTF8"));
            } catch (Exception e) {
                localConfig = null;
                throw new RuntimeException(e);
            } finally {
                if (resourceStream != null) try {
                    resourceStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (fileConfig == null) try {
            // try to read config file
            fileConfig = new YamlConfiguration();
            File file = new File(VarScript.dataFolder, "config.yml");
            if (file.isFile()) fileConfig.load(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getConfigString(String path, String defValue){
        loadConfigs();
        String value = fileConfig.getString(path);
        return value != null ? value : localConfig.getString(path, defValue);
    }

    private static int getConfigInt(String path, int defValue){
        loadConfigs();
        Object value = fileConfig.get(path);
        return value != null ? NumberConversions.toInt(value) : localConfig.getInt(path, defValue);
    }

    public static void prepareSystemVariables(){
        System.setProperty("ivy.message.logger.level",getConfigString("ivy.log-level","4"));
        String confHome = getConfigString("system.user.home",null);
        if (confHome != null) {
            System.setProperty("user.home",confHome);
        } else if (System.getProperty("user.home").equals("?")){
            System.setProperty("user.home",new File("").getAbsolutePath());
        }
    }

    /**
     * Configure Groovy Grape engine to use local ivy instance
     */
    public static void configureGrape(){
        GrapeIvy grape = (GrapeIvy) Grape.getInstance();
        IvySettings settings = new IvySettings();
        ClassLoader resourceClassLoader = VarScript.class.getClassLoader();
        File ivySettingsXmlFile = new File(VarScript.dataFolder, "ivysettings.xml");
        try {
            if (ivySettingsXmlFile.isFile()) settings.load(ivySettingsXmlFile);
            else settings.load(resourceClassLoader.getResource("ivysettings.xml"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        grape.setIvyInstance(ivy);
        grape.setSettings(settings);
    }

    /**
     * Download (if needed) and load all dependencies from ivy.xml of this plugin
     */
    public static void loadSelfDependencies(){
        ClassLoader resourceClassLoader = VarScript.class.getClassLoader();
        File ivyXmlFile = new File(VarScript.dataFolder, "ivy.xml");
        try {
            ResolveReport report;
            if (ivyXmlFile.isFile()) report = resolveIvy(ivyXmlFile);
            else report = resolveIvy(resourceClassLoader.getResource("ivy.xml"));
            loadReportedArtifacts(report, VarScript.pluginName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadLibraries(){
        ClassLoader resourceClassLoader = VarScript.class.getClassLoader();
        VarScriptClassLoader libLoader = VarScript.libLoader;
        File configFile = new File(VarScript.dataFolder, "config.yml");
        Map config = null;
        if (configFile.isFile()) config = readYaml(configFile);
        Object librariesValue = null;
        if (config != null) librariesValue = config.get("libraries");
        if (librariesValue == null) {
            config = readYaml(resourceClassLoader.getResource("config.yml"));
            librariesValue = config.get("libraries");
        }
        if (librariesValue instanceof String && !((String) librariesValue).isEmpty()) {
            File librariesFolder = new File((String) librariesValue);
            File[] files = librariesFolder.listFiles();
            if (!librariesFolder.exists()) librariesFolder.mkdirs();
            if (!librariesFolder.isDirectory()) return;
            libLoader.addLibFile(librariesFolder);
            if (files != null) for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".jar")) {
                    libLoader.addLibFile(file, VarScriptClassLoader.TO_PARENT);
                }
            }
        }
    }

    public static void loadExtensions(ClassLoader classLoader){
        Map<CachedClass, List<MetaMethod>> map = new HashMap<CachedClass, List<MetaMethod>>();
        Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(ExtensionModuleScanner.MODULE_META_INF_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(resources.hasMoreElements()){
            URL url = resources.nextElement();
            Properties properties = new Properties();
            InputStream inputStream = null;
            MetaClassRegistryImpl registry = (MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry();
            try {
                inputStream = url.openConnection().getInputStream();
                properties.load(inputStream);
                registry.registerExtensionModuleFromProperties(properties, classLoader, map);
            } catch (IOException ignored){
            } finally {
                if (inputStream != null) try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        for (Map.Entry<CachedClass, List<MetaMethod>> entry : map.entrySet()) {
            CachedClass cls = entry.getKey();
            List<MetaMethod> methods = entry.getValue();
            cls.setNewMopMethods(methods);
        }
    }

    /**
     * Download ivy.jar from URL to file
     *
     * @param urlStr target URL
     * @param targetFile target file
     */
    private static void downloadIvyJar(String urlStr, File targetFile){
        InputStream input = null;
        FileOutputStream fos = null;
        try {
            targetFile.getParentFile().mkdirs();
            input = URI.create(urlStr).toURL().openStream();
            fos = new FileOutputStream(targetFile);
            pipe(input, fos, 0x1000);
        } catch (Exception e) {
            throw new RuntimeException("Can not download ivy: "+urlStr,e);
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException ignored) {
            }
            if (fos != null) try {
                fos.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Copy inner resource to file
     *
     * @param resource path to resource
     * @param file target file
     */
    static void copyResource(String resource, File file){
        file.getParentFile().mkdirs();
        FileOutputStream fos = null;
        InputStream is = null;
        try{
            fos = new FileOutputStream(file);
            is = VarScript.class.getClassLoader().getResourceAsStream(resource);
            if (is != null) pipe(is, fos, 0x100);
        } catch (IOException e) {
            throw new RuntimeException("Can not copy resource "+resource,e);
        } finally {
            if (fos != null) try {
                fos.close();
            } catch (IOException ignored) {
            }
            if (is != null) try {
                is.close();
            } catch (IOException ignored) {
            }
        }

    }

    /**
     * Copy all data from input to output
     *
     * @param input input
     * @param output output
     * @param bufferSize buffer size
     * @throws IOException on any IO error
     */
    private static void pipe(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int len;
        while (true) {
            len = input.read(buffer);
            if ( len <= 0) break;
            output.write(buffer, 0, len);
        }
    }

    private static Yaml yaml = new Yaml();

    /**
     * Check all plugins for ivy dependencies
     */
    public static void loadAllPluginsIvyDependencies(){
        for (File file : getAllPluginsFiles()) {
            loadPluginIvyDependencies(file);
        }
    }

    /**
     * Get all jar files in plugins folder
     */
    private static List<File> getAllPluginsFiles(){
        List<File> result = new ArrayList<File>();
        File[] plFiles = new File("plugins").listFiles();
        if (plFiles != null) for (File file : plFiles) {
            if (!file.getName().toLowerCase().endsWith(".jar")) continue;
            if (!file.isFile()) continue;
            result.add(file);
        }
        return result;
    }

    /**
     * Check ivy dependencies for plugin
     *
     * @param pluginFile plugin file
     */
    private static void loadPluginIvyDependencies(File pluginFile){

        try {
            URL pluginYmlResource = getResourceURL(pluginFile, "plugin.yml");
            Map pluginYml = readYaml(pluginYmlResource);
            if (!isDependOnThisPlugin(pluginYml)) return;
            String pluginName = (String) pluginYml.get("name");
            File pluginFolder = new File(VarScript.pluginsFolder, pluginName);
            File ivyXmlFile = new File(pluginFolder, "ivy.xml");
            ResolveReport report;
            if (ivyXmlFile.isFile()) {
                report = resolveIvy(ivyXmlFile);
            } else {
                URL ivyXmlResource = getResourceURL(pluginFile,"ivy.xml");
                report = resolveIvy(ivyXmlResource);
            }
            loadReportedArtifacts(report, pluginName);
        } catch (Exception exception){
            String msg = VarScript.pluginName + ": can not load dependencies of plugin " + pluginFile.getName();
            Bukkit.getLogger().log(Level.WARNING, msg, exception);
        }
    }

    /**
     * load all downloaded jars to classloader
     *
     * @param report ivy report
     * @param pluginName name of plugin
     */
    private static void loadReportedArtifacts(ResolveReport report, String pluginName){
        VarScriptClassLoader libLoader = VarScript.libLoader;
        if (report.hasError()){
            List problems = report.getAllProblemMessages();
            String message = "Error on load dependencies of "+ pluginName +"\n" + problems;
            Bukkit.getLogger().log(Level.WARNING, VarScript.pluginName+": " +message);
        }
        for (ArtifactDownloadReport r: report.getAllArtifactsReports()) {
            libLoader.addLibFile(r.getLocalFile(), VarScriptClassLoader.TO_PARENT);
        }
    }

    /**
     * Get URL to inner resource in jar file
     *
     * @param file file
     * @param resource resource path
     * @return URL
     */
    private static URL getResourceURL(File file, String resource) {
        try { return new URL("jar:file:"+file+"!/"+resource);}
        catch (MalformedURLException e) {return null;}
    }

    /**
     * Read yaml map from url
     *
     * @param url url
     * @return parsed map
     */
    private static Map readYaml(URL url){
        InputStream is = null;
        try{
            is = url.openStream();
            Reader reader = new InputStreamReader(is,"UTF8");
            return (Map) yaml.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Can not load yaml: "+url,e);
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read yaml map from file
     * @param file file
     * @return parsed map
     */
    private static Map readYaml(File file){
        InputStream is = null;
        try{
            is = new FileInputStream(file);
            Reader reader = new InputStreamReader(is,"UTF8");
            return (Map) yaml.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Can not load yaml: "+file, e);
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check that yaml config has dependencies on this plugin
     *
     * @param pluginYml parsed yaml config
     * @return boolean
     */
    private static boolean isDependOnThisPlugin(Map pluginYml) {
        Object depend = pluginYml.get("depend");
        Object softDepend = pluginYml.get("softdepend");
        return depend instanceof List && ((List) depend).contains(VarScript.pluginName) ||
                softDepend instanceof List && ((List) softDepend).contains(VarScript.pluginName);

    }

    /**
     * Resolve ivy dependencies by url
     *
     * @param ivySource url
     * @return ivy resolve report
     * @throws IOException
     * @throws ParseException
     */
    private static ResolveReport resolveIvy(URL ivySource) throws IOException, ParseException {
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(new String[]{"default"})
                .setOutputReport(false)
                .setValidate(true);
        return ivy.resolve(ivySource, resolveOptions);
    }

    /**
     * Resolve ivy dependencies by ivy.xml file
     *
     * @param ivySource ivy.xml
     * @return ivy resolve report
     * @throws IOException
     * @throws ParseException
     */
    private static ResolveReport resolveIvy(File ivySource) throws IOException, ParseException {
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(new String[]{"default"})
                .setOutputReport(false)
                .setValidate(true);
        return ivy.resolve(ivySource, resolveOptions);
    }

    /**
     * Resolve ivy dependencies by module revision
     *
     * @param mrId module revision
     * @param conf resolve configuration
     * @return ivy resolve report
     * @throws IOException
     * @throws ParseException
     */
    private static ResolveReport resolveIvy(ModuleRevisionId mrId, String conf) throws IOException, ParseException {
        ResolveOptions resolveOptions = new ResolveOptions()
                .setConfs(new String[]{conf})
                .setOutputReport(false)
                .setValidate(true);
        return ivy.resolve(mrId, resolveOptions, true);
    }

    /**
     * Resolve ivy dependencies by module description
     *
     * @param mrId module description
     * @param conf resolve configuration
     * @return ivy resolve report
     * @throws IOException
     * @throws ParseException
     */
    public static ResolveReport resolveIvy(String mrId, String conf) throws IOException, ParseException {
        return resolveIvy(ModuleRevisionId.parse(mrId), conf);
    }

    /**
     * Scan class names in package
     * @param packageName name
     * @return class names in package
     */
    public static ArrayList<String> getClassNamesFromPackage(String packageName, boolean recursive) {
        ClassLoader resourceClassLoader = VarScript.class.getClassLoader();
        URL packageURL;
        ArrayList<String> names = new ArrayList<String>();

        packageName = packageName.replace('.','/');
        if (!packageName.endsWith("/")) packageName += '/';
        packageURL = resourceClassLoader.getResource(packageName);

        if( packageURL != null && packageURL.getProtocol().equals("jar")) try {
            // build jar file name, then loop through zipped entries
            String jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
            jarFileName = jarFileName.substring(5,jarFileName.indexOf("!"));
            JarFile jf = new JarFile(jarFileName);
            Enumeration<JarEntry> jarEntries = jf.entries();
            while (jarEntries.hasMoreElements()){
                JarEntry entry = jarEntries.nextElement();
                if (entry.isDirectory()) continue;
                String entryName = entry.getName();
                if (!entryName.startsWith(packageName)) continue;
                if (!entryName.endsWith(".class")) continue;
                if (entryName.contains("$")) continue;
                if (!recursive) {
                    String end = entryName.substring(packageName.length());
                    if (end.contains("/")) continue;
                }
                String name = entryName.substring(0,entryName.length()-6);
                String className = name.replace('/','.');
                names.add(className);
            }
        } catch (IOException ignored) {}
        return names;
    }


}
