package ru.dpohvar.varscript.groovy.extension

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.entity.LightningStrike
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector
import ru.dpohvar.varscript.Workspace
import ru.dpohvar.varscript.event.EventHandler
import ru.dpohvar.varscript.groovy.BukkitExtUtils;

/**
 * Created by DPOH-VAR on 06.03.14
 */

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
class ScriptExtension<Z extends Script> {

    /**
     * get current location of "me"
     * @param self
     * @return location
     */
    public static Location here(Z self) {
        def me = self.binding.getProperty("me")
        switch (me) {
            case Entity:
                return ((Entity)me).location
            case BlockCommandSender:
                return ((BlockCommandSender)me).block.location
            default:
                try {
                    getStaticLocationTag self
                } catch (e) {
                    throw new RuntimeException("$me has no location", e)
                }
        }

    }

    @CompileStatic(TypeCheckingMode.SKIP)
    private static Location getStaticLocationTag(Z self) {
        self.binding.getProperty("me").location as Location
    }

    /**
     * get current location of "me"
     * @param self
     * @return location
     */
    public static Location getHere(Z self){
        here self
    }

    public static CommandSender getMeAsCommandSender(Z self) {
        try{
            self.binding.getProperty("me") as CommandSender
        } catch (ignored) {
            null
        }
    }

    public static Player getMeAsPlayer(Z self) {
        try{
            self.binding.getProperty("me") as Player
        } catch (ignored) {
            null
        }
    }

    public static Block getTarb(Z self) {
        getMeAsPlayer self getTargetBlock null, 32
    }

    public static Entity getTar(Z self) {
        LivingEntityExtension.getTar getMeAsPlayer(self)
    }

    public static Location loc(Z self, double x, double y, double z, String world) {
        new Location(Bukkit.getWorld(world),x,y,z)
    }

    public static Location loc(Z self, double x, double y, double z, World world) {
        new Location(world,x,y,z)
    }

    public static Location loc(Z self, double x, double y, double z) {
        new Location(here(self).world,x,y,z)
    }

    public static Vector vector(Z self, double x, double y, double z) {
        new Vector(x, y, z)
    }

    public static Vector vector(Z self, double y) {
        new Vector(0, y, 0)
    }

    public static Vector up(Z self, double y) {
        new Vector(0, y, 0)
    }

    public static Vector down(Z self, double y) {
        new Vector(0, -y, 0)
    }

    public static ItemStack item(Z self, int id, int data=0, int amount=1){
        BukkitExtUtils.item(id, data, amount)
    }

    public static ItemStack item(Z self, Material material, int data=0, int amount=1){
        BukkitExtUtils.item(material, data, amount)
    }

    public static ItemStack item(Z self, String material, int data=0, int amount=1){
        BukkitExtUtils.item(material, data, amount)
    }

    // sweet methods

    public static <T extends Damageable> T kill (Z self, T damageable) {
        damageable.health = 0
        damageable
    }

    public static <T extends Damageable> T heal (Z self, T damageable){
        damageable.setHealth(damageable.maxHealth)
        damageable
    }

    public static <T extends Player> T kick (Z self, T player, String reason="") {
        player.kickPlayer reason
        player
    }

    public static Player kick (Z self, String name, String reason="") {
        kick self, Bukkit.getPlayer(name), reason
    }

    public static Player player (Z self, String name) {
        Bukkit.getPlayer(name)
    }

    public static BukkitExtUtils.Teleporter teleport (Z self, List<? extends Entity> e) {
        new BukkitExtUtils.Teleporter(entities:e, caller: here(self))
    }

    public static BukkitExtUtils.Teleporter teleport (Z self, Entity... e) {
        new BukkitExtUtils.Teleporter(entities:e as List, caller: here(self))
    }

    public static BukkitExtUtils.Teleporter tp (Z self, List<? extends Entity> e) {
        teleport self, e
    }

    public static BukkitExtUtils.Teleporter tp (Z self, Entity... e) {
        teleport self, e
    }

    public static LightningStrike bolt (Z self, Location loc){
        loc.world.strikeLightning(loc)
    }

    public static LightningStrike bolt (Z self, Entity some){
        bolt self, some.location
    }

    public static LightningStrike bolt (Z self, Block some){
        bolt self, some.location
    }

    public static LightningStrike fbolt (Z self, Location loc){
        loc.world.strikeLightningEffect loc
    }

    public static LightningStrike fbolt (Z self, Entity some){
        fbolt self, some.location
    }

    public static LightningStrike fbolt (Z self, Block some){
        fbolt self, some.location
    }

    public static void bc (Z self, Object message) {
        Bukkit.broadcastMessage "$message"
    }

    public static void bc (Z self, Object... messages) {
        messages.each { bc self, it}
    }

    public static List<Player> getPlayers (Z self) {
        Bukkit.onlinePlayers as List
    }

    public static List<OfflinePlayer> getOfflinePlayers (Z self) {
        Bukkit.offlinePlayers as List
    }

    public static List<Entity> getEntities (Z self) {
        BukkitExtUtils.entities
    }

    public static List<Entity> getItems (Z self) {
        BukkitExtUtils.items
    }

    public static List<Entity> getMobs (Z self) {
        BukkitExtUtils.mobs
    }

    public static List<Entity> getLiving (Z self) {
        BukkitExtUtils.living
    }

    // ========= FOR PLAYER ONLY =========

    public static GameMode getGm(Z self) {
        getMeAsPlayer self gameMode
    }

    public static GameMode gm(Z self) {
        getMeAsPlayer self gameMode
    }

    public static void gm(Z self, GameMode gm) {
        getMeAsPlayer(self).gameMode = gm
    }

    public static void gm(Z self, int gm) {
        getMeAsPlayer(self).gameMode = GameMode.getByValue gm
    }

    public static void gm(Z self, String gm) {
        getMeAsPlayer(self).gameMode = BukkitExtUtils.parseEnum GameMode, gm
    }

    public static <T extends Entity> T tphere (Z self, T entity) {
        entity.teleport(here(self))
        entity
    }

    public static Z tpto (Z self, Location loc) {
        getMeAsPlayer self teleport loc
        self
    }

    public static Z tpto (Z self, Entity some) {
        tpto self, some.location
    }

    public static Z tpto (Z self, Block some) {
        tpto self, some.location
    }

    public static Z tpto (Z self, Vector v) {
        tpto self, here(self).add(v)
    }

    public static Entity spawn (Z self, EntityType type){
        here(self).world.spawnEntity here(self), type
    }

    public static <T extends Entity> T spawn (Z self, Class<T> type){
        here(self).world.spawn here(self), type
    }

    public static <T extends Entity> T spawn (Z self, T type){
        spawn self, type.type
    }

    public static Entity spawn (Z self, String type){
        spawn self, BukkitExtUtils.parseEnum(EntityType, type)
    }

    public static FallingBlock spawn (Z self, Material material, int data=0){
        def loc = getMeAsPlayer self location
        loc.world.spawnFallingBlock loc, material, data as byte
    }

    public static FallingBlock spawn (Z self, int material, int data=0){
        here(self).world.spawnFallingBlock here(self), material, data as byte
    }

    public static FallingBlock spawn (Z self, String material, int data){
        here(self).world.spawnFallingBlock here(self), BukkitExtUtils.parseEnum(Material, material), data as byte
    }

    public static Item spawn (Z self, ItemStack item){
        here(self).world.dropItem here(self), item
    }

    // events

    public static Workspace getWorkspace(Z self){
        self.binding.getAt("workspace") as Workspace
    }


    public static BukkitExtUtils.EventTrigger register(Z self, Closure closure) {
        Class[] params = closure.parameterTypes
        assert params.length == 1, "wrong number of arguments in closure"
        Class eventClass = params[0]
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = getWorkspace(self).addEvent closure as EventHandler, eventClass
        new BukkitExtUtils.EventTrigger(id: id, workspace: getWorkspace(self))
    }

    public static BukkitExtUtils.EventTrigger register(Z self, Class<? extends Event> eventClass, Closure closure) {
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = getWorkspace(self).addEvent closure as EventHandler<? extends Event>, eventClass
        new BukkitExtUtils.EventTrigger(id: id, workspace: getWorkspace(self))
    }

    public static BukkitExtUtils.EventTrigger register(Z self, String eventClassName, Closure closure) {
        register self, BukkitExtUtils.getEventClass(eventClassName), closure
    }

    public static BukkitExtUtils.EventTrigger listen(Z self, Closure closure) {
        Class[] params = closure.parameterTypes
        assert params.length == 1, "wrong number of arguments in closure"
        Class eventClass = params[0]
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = getWorkspace(self).addEvent closure as EventHandler, eventClass, true
        new BukkitExtUtils.EventTrigger(id: id, workspace: getWorkspace(self))
    }

    public static BukkitExtUtils.EventTrigger listen(Z self, Class<? extends Event> eventClass, Closure closure) {
        assert Event.isAssignableFrom(eventClass), "listen to not event class"
        long id = getWorkspace(self).addEvent closure as EventHandler<? extends Event>, eventClass, true
        new BukkitExtUtils.EventTrigger(id: id, workspace: getWorkspace(self))
    }

    public static BukkitExtUtils.EventTrigger listen(Z self, String eventClassName, Closure closure) {
        listen self, BukkitExtUtils.getEventClass(eventClassName), closure
    }

    /**
     * run closure synchronously after delay
     * @param self
     * @param ticks delay in ticks
     * @param closure
     * @return trigger
     */
    public static BukkitExtUtils.DelayTrigger delay(Z self, long ticks, Closure closure) {
        long id = getWorkspace(self).addDelay closure, ticks
        new BukkitExtUtils.DelayTrigger(id: id, workspace: getWorkspace(self))
    }

    /**
     * run closure synchronously every N ticks
     * @param self
     * @param ticks period in ticks
     * @param closure
     * @return trigger
     */
    public static BukkitExtUtils.PeriodTrigger period(Z self, long ticks, Closure closure) {
        long id = getWorkspace(self).addPeriod closure, ticks
        new BukkitExtUtils.PeriodTrigger(id: id, workspace: getWorkspace(self))
    }

    /**
     * run closure asynchronously after delay
     * @param self
     * @param ticks delay in milliseconds
     * @param closure
     * @return trigger
     */
    public static BukkitExtUtils.AsyncDelayTrigger wait(Z self, long millis, Closure closure) {
        long id = getWorkspace(self).addAsyncDelay closure, millis
        new BukkitExtUtils.AsyncDelayTrigger(id: id, workspace: getWorkspace(self))
    }

    /**
     * run closure asynchronously every N milliseconds
     * @param self
     * @param ticks period in milliseconds
     * @param closure
     * @return trigger
     */
    public static BukkitExtUtils.AsyncPeriodTrigger timer(Z self, long millis, Closure closure) {
        long id = getWorkspace(self).addAsyncPeriod closure, millis
        new BukkitExtUtils.AsyncPeriodTrigger(id: id, workspace: getWorkspace(self))
    }

    /**
     * run closure when workspace is stopped
     * @param self
     * @param closure
     * @return trigger
     */
    public static BukkitExtUtils.FinisherTrigger onStop(Z self, Closure closure) {
        long id = getWorkspace(self).addFinisher closure
        new BukkitExtUtils.FinisherTrigger(id: id, workspace: getWorkspace(self))
    }

}
