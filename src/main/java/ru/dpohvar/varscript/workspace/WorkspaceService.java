package ru.dpohvar.varscript.workspace;

import groovy.lang.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import ru.dpohvar.varscript.VarScript;
import ru.dpohvar.varscript.modifier.ImportVersionASTTransformation;
import ru.dpohvar.varscript.modifier.SourceASTTransformationCustomizer;
import ru.dpohvar.varscript.utils.FileTime;

import org.bukkit.entity.*;
import org.bukkit.block.*;

import java.io.File;
import java.util.*;

public class WorkspaceService extends GroovyObjectSupport {

    private static boolean usePlayerUniqueId;
    private static boolean useEntityUniqueId;
    static {
        try {
            OfflinePlayer.class.getMethod("getUniqueId");
            usePlayerUniqueId = true;
        } catch (NoSuchMethodException e) {
            usePlayerUniqueId = false;
        }
        try {
            Entity.class.getMethod("getUniqueId");
            useEntityUniqueId = true;
        } catch (NoSuchMethodException e) {
            useEntityUniqueId = false;
        }
    }

    public static Object getSenderHashKey(CommandSender sender){
        if (sender instanceof OfflinePlayer && usePlayerUniqueId) {
            return ((OfflinePlayer) sender).getUniqueId();
        } else if (sender instanceof Entity && useEntityUniqueId) {
            return ((Entity) sender).getUniqueId();
        } else if (sender instanceof BlockCommandSender) {
            return ((BlockCommandSender) sender).getBlock();
        }
        return sender.getName();
    }

    private VarScript VarScript;

    private final File autorunDirectory;
    private final File scriptsDirectory;
    private final Map<String,Workspace> workspaceMap = new HashMap<String, Workspace>();
    private final List<CompilationCustomizer> compilationCustomizers = new LinkedList<CompilationCustomizer>();
    private final List<String> classPath = new LinkedList<String>();
    private final List<GroovyObject> dynamicModifiers = new ArrayList<GroovyObject>();
    private final Map<FileTime,Class> compiledFileTimeCache = new WeakHashMap<FileTime, Class>();

    private Binding binding = new Binding();

    private static void fillImports(ImportCustomizer customizer, Class... classes){
        for (Class clazz : classes) {
            customizer.addImport(clazz.getSimpleName(), clazz.getName());
        }
    }

    public WorkspaceService(VarScript VarScript){
        this.VarScript = VarScript;
        this.autorunDirectory = new File(VarScript.getDataFolder(), "autorun");
        this.scriptsDirectory = new File(VarScript.getDataFolder(), "scripts");
        ImportCustomizer importCustomizer = new ImportCustomizer();
        compilationCustomizers.add(importCustomizer);
        ImportVersionASTTransformation astTransformation = new ImportVersionASTTransformation();
        CompilationCustomizer nmsCustomizer = new SourceASTTransformationCustomizer(astTransformation);
        compilationCustomizers.add(nmsCustomizer);
        importCustomizer.addImport("Vector", "org.bukkit.util.Vector");
        fillImports( importCustomizer,
                Ageable.class,
                Ambient.class,
                Animals.class,
                AnimalTamer.class,
                ArmorStand.class,
                Arrow.class,
                Bat.class,
                Blaze.class,
                Boat.class,
                CaveSpider.class,
                Chicken.class,
                ComplexEntityPart.class,
                ComplexLivingEntity.class,
                Cow.class,
                Creature.class,
                Creeper.class,
                Damageable.class,
                Egg.class,
                EnderCrystal.class,
                EnderDragon.class,
                EnderDragonPart.class,
                Enderman.class,
                Endermite.class,
                EnderPearl.class,
                EnderSignal.class,
                Entity.class,
                EntityType.class,
                ExperienceOrb.class,
                Explosive.class,
                FallingBlock.class,
                Fireball.class,
                Firework.class,
                FishHook.class,
                Flying.class,
                Ghast.class,
                Giant.class,
                Golem.class,
                Guardian.class,
                Hanging.class,
                Horse.class,
                HumanEntity.class,
                IronGolem.class,
                Item.class,
                ItemFrame.class,
                LargeFireball.class,
                LeashHitch.class,
                LightningStrike.class,
                LivingEntity.class,
                MagmaCube.class,
                Minecart.class,
                Monster.class,
                MushroomCow.class,
                NPC.class,
                Ocelot.class,
                Painting.class,
                Pig.class,
                PigZombie.class,
                Player.class,
                Projectile.class,
                Rabbit.class,
                Sheep.class,
                Silverfish.class,
                Skeleton.class,
                Slime.class,
                SmallFireball.class,
                Snowball.class,
                Snowman.class,
                Spider.class,
                Squid.class,
                Tameable.class,
                ThrownExpBottle.class,
                ThrownPotion.class,
                TNTPrimed.class,
                Vehicle.class,
                Villager.class,
                WaterMob.class,
                Weather.class,
                Witch.class,
                Wither.class,
                WitherSkull.class,
                Wolf.class,
                Zombie.class,
                Beacon.class,
                Biome.class,
                Block.class,
                BlockFace.class,
                BlockState.class,
                BrewingStand.class,
                CommandBlock.class,
                ContainerBlock.class,
                CreatureSpawner.class,
                DoubleChest.class,
                Dropper.class,
                Hopper.class,
                Jukebox.class,
                NoteBlock.class,
                PistonMoveReaction.class
        );
//        importCustomizer.addStarImports(
//                "org.bukkit",
//                "org.bukkit.block",
//                "org.bukkit.enchantments",
//                "org.bukkit.entity",
//                "org.bukkit.entity.minecart",
//                "org.bukkit.event",
//                "org.bukkit.event.block",
//                "org.bukkit.event.enchantment",
//                "org.bukkit.event.entity",
//                "org.bukkit.event.hanging",
//                "org.bukkit.event.inventory",
//                "org.bukkit.event.painting",
//                "org.bukkit.event.player",
//                "org.bukkit.event.server",
//                "org.bukkit.event.vehicle",
//                "org.bukkit.event.weather",
//                "org.bukkit.event.world",
//                "org.bukkit.inventory",
//                "org.bukkit.inventory.meta",
//                "org.bukkit.material",
//                "org.bukkit.metadata",
//                "org.bukkit.permissions",
//                "org.bukkit.plugin",
//                "org.bukkit.potion",
//                "org.bukkit.projectiles",
//                "org.bukkit.scoreboard",
//                "org.bukkit.util"
//        );
        classPath.add(scriptsDirectory.toString());
    }

    public List<GroovyObject> getDynamicModifiers() {
        return dynamicModifiers;
    }

    public void startAutorun(){
        File[] files = autorunDirectory.listFiles();
        if (files != null) for (File file : files) {
            String name = file.getName();
            if (!name.endsWith(".groovy")) continue;
            getOrCreateWorkspace(name.substring(0, name.length()-7));
        }
    }

    public VarScript getVarScript() {
        return VarScript;
    }

    public File getScriptsDirectory() {
        return scriptsDirectory;
    }

    public Binding getBinding() {
        return binding;
    }

    public File getAutorunDirectory(){
        return autorunDirectory;
    }

    public List<CompilationCustomizer> getCompilationCustomizers() {
        return compilationCustomizers;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public String getWorkspaceName(CommandSender sender){
        FileConfiguration config = VarScript.getConfig();
        String workspaceName = config.getString("workspace." + sender.getName());
        if (workspaceName != null) return workspaceName;
        else return sender.getName();
    }

    public void setWorkspaceName(CommandSender sender, String workspaceName){
        FileConfiguration config = VarScript.getConfig();
        config.set("workspace." + sender.getName(), workspaceName);
        VarScript.saveConfig();
    }

    public Workspace getWorkspace(String workspaceName) {
        return workspaceMap.get(workspaceName);
    }

    public boolean hasWorkspace(Workspace workspace){
        return workspaceMap.get(workspace.getName()) == workspace;
    }

    public Workspace getOrCreateWorkspace(String workspaceName) {
        Workspace workspace = workspaceMap.get(workspaceName);
        if (workspace != null) return workspace;
        workspace = new Workspace(this, workspaceName);
        workspaceMap.put(workspaceName, workspace);
        workspace.doAutorun();
        return workspace;
    }

    public void remove(Workspace workspace) {
        if (!workspace.isRemoved()) throw new IllegalArgumentException("Workspace is not disabled");
        String name = workspace.getName();
        if (workspaceMap.get(name) != workspace) throw new IllegalArgumentException("Workspace is not registered");
        workspaceMap.remove(name);
    }

    public Workspace[] getWorkspaces(){
        Collection<Workspace> values = workspaceMap.values();
        Workspace[] result = new Workspace[values.size()];
        return values.toArray(result);
    }

    @Override
    public Object getProperty(String property) {
        return binding.getVariable(property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        binding.setVariable(property, newValue);
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        Object[] arguments;
        if (args instanceof Object[]) arguments = (Object[]) args;
        else arguments = new Object[]{args};

        try {
            return getMetaClass().invokeMethod(this, name, args);
        } catch (MissingMethodException ignored){}

        try {
            Object variable = binding.getVariable(name);
            if (variable instanceof Closure) return ((Closure) variable).call(arguments);
            else InvokerHelper.invokeMethod(variable, "call", arguments);
        }
        catch (MissingMethodException ignored){}
        catch (MissingPropertyException ignored){}

        throw new MissingMethodException(name, this.getClass(), arguments);
    }

    public Class getCompiledFileTimeCache(FileTime fileTime){
        return compiledFileTimeCache.get(fileTime);
    }

    public Class setCompiledFileTimeCache(FileTime fileTime, Class cache){
        return compiledFileTimeCache.put(fileTime, cache);
    }
}



























