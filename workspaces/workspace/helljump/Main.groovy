package helljump

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.util.Vector
import ru.dpohvar.varscript.VarScriptPlugin
import ru.dpohvar.varscript.Workspace
import ru.dpohvar.varscript.event.EventHandler

import static org.bukkit.ChatColor.*
import static ru.dpohvar.varscript.utils.ReflectionUtils.*

/**
 * HellJump
 * @author DPOH-VAR
 * @version 2.2
 */

public class HelljumpArena {
    private int basicStartDelay = 10
    private Set<Closure> endGameHandlers = [] as Set
    private static String prefix = "${RED}${BOLD}[${YELLOW}Hell${BOLD}j${YELLOW}ump${RED}${BOLD}]${RESET}"
    private final Workspace workspace
    private String name
    private HelljumpField field
    private boolean gameStarted = false
    private List<Player> players = []
    private Map<Player, PlayerData> playerDatas = [:]
    private int platformCount
    private boolean removed = false
    private Location center
    private int radius
    private Collection<Block> blocks
    private int startDelay = 0
    private basicLevelLimit = 1000
    private levelScoreLimit

    public int getBasicStartDelay() {
        return basicStartDelay
    }

    public void setBasicStartDelay(int basicStartDelay) {
        this.basicStartDelay = basicStartDelay
    }

    public void addEndGameHandler(Closure handler) {
        endGameHandlers << handler
    }

    public boolean removeEndGameHandler(Closure handler) {
        endGameHandlers.remove handler
    }

    private long bombingDelay = -1
    private long startCountPeriod = -1
    private long changeTimePeriod = -1
    private long blockBreakTrigger = -1
    private long blockInteractTrigger = -1
    private long playerDamageTrigger = -1
    private long playerScanPeriod = -1
    private long explodeTrigger = -1
    private long finisher = -1
    private long gameModeTrigger = -1
    private long leaveTrigger = -1
    private long vehicleEnterTrigger = -1

    private Scores scores = new Scores()
    private int score
    private int level
    private int time

    public HelljumpArena(String name, Location center, double radius, Workspace ws = null) {
        this.name = name
        this.radius = radius
        this.center = center
        if (ws == null) ws = VarScriptPlugin.plugin.workspaceManager.getWorkspace "${name}@helljump"
        workspace = ws
        this.field = new HelljumpField(center, radius, workspace)
        this.platformCount = field.platforms.size()
        assert platformCount > 1, "platforms must be > 1"
        this.blocks = field.blocks

        blockInteractTrigger = workspace.addEvent({ PlayerInteractEvent event ->
            Block b = event.clickedBlock
            if (b in blocks) event.cancelled = true
            Player player = event.player
            if (player in players) event.cancelled = true
        } as EventHandler, PlayerInteractEvent.class)

        blockBreakTrigger = workspace.addEvent({ BlockBreakEvent event ->
            Block b = event.block
            if (b in blocks) event.cancelled = true
        } as EventHandler, BlockBreakEvent.class)

        explodeTrigger = workspace.addEvent({ EntityExplodeEvent event ->
            List<Block> exploded = event.blockList()
            exploded.removeAll(blocks)
        } as EventHandler, EntityExplodeEvent.class)

        finisher = workspace.addFinisher({
            if (!removed) remove()
        })
    }

    private long getFuseTime() {
        1 / (1d / 80d + (level - 1) / 800d) as long
    }

    private long getPlanRegenTime() {
        fuseTime * 2 + 10 as long
    }

    private long getCreateBombTime() {
        1 / platformCount / (1d / 400d + (level - 1) / 3000d) as long
    }

    private long getRandomCreateBombTime() {
        long t = createBombTime
        t * 0.5 + t * Math.random() as long
    }

    public boolean play(Player... players) {
        play players as List
    }

    public boolean play(List<Player> players) {
        if (removed) return false
        players.removeAll(this.players)
        if (!players) return false
        int total = this.players.size() + players.size()
        if (total > platformCount) {
            players.each { it.sendMessage("$prefix$RED Too many players:$total (limit:$platformCount)") }
            return false
        }
        if (gameStarted && startDelay <= 0) {
            players.each { it.sendMessage("$prefix$RED game already started") }
            return false
        }
        players.each {
            playerDatas.put(it, new PlayerData(it, workspace))
            it.sendMessage("$prefix$AQUA Let's Helljump in $GREEN$name$AQUA!")
            for (def e in it.activePotionEffects) it.removePotionEffect(e.type)
            it.gameMode = GameMode.ADVENTURE
            it.foodLevel = 10000
        }
        field.settlePlayers players
        scores.showTo players
        this.players += players
        if (!gameStarted) start()

        return true
    }

    private void start() {
        this.gameStarted = true
        this.startDelay = this.basicStartDelay
        score = 0
        time = 0
        level = 0
        levelScoreLimit = 0
        scores.reset()

        startCountPeriod = workspace.addPeriod({
            if (startDelay > 0) {
                scores.startTime = (startDelay--)
            } else {
                workspace.stopPeriod(startCountPeriod)
                scores.reset()
                changeTimePeriod = workspace.addPeriod({ increaseTimer() }, 20, 0)
                bombing()
            }
        }, 20, 0)

        playerScanPeriod = workspace.addPeriod({ scanPlayers() }, 20)

        playerDamageTrigger = workspace.addEvent({ EntityDamageEvent event ->
            onEntityDamage event
        } as EventHandler, EntityDamageEvent.class)

        gameModeTrigger = workspace.addEvent({ PlayerGameModeChangeEvent event ->
            Player player = event.player
            if (player in players) leave player
        } as EventHandler, PlayerGameModeChangeEvent.class)

        leaveTrigger = workspace.addEvent({ PlayerQuitEvent event ->
            Player player = event.player
            if (player in players) leave player
        } as EventHandler, PlayerQuitEvent.class)

        vehicleEnterTrigger = workspace.addEvent({ PlayerInteractEntityEvent event ->
            Player player = event.player
            if (player in players) event.cancelled = true
        } as EventHandler, PlayerInteractEntityEvent.class)

        field.reset()
    }

    private void onEntityDamage(EntityDamageEvent event) {
        if (event.entityType != EntityType.PLAYER) return
        Player player = (Player) event.entity
        if (!(player in players)) return
        switch (event.cause) {
            case EntityDamageEvent.DamageCause.LAVA:
                leave player
                break
            case EntityDamageEvent.DamageCause.FALL:
                def stayPlatform = field.platforms.find {
                    it.center.world == player.world &&
                            it.center.distance(player.location) < 2
                }
                if (!stayPlatform) leave player
                break
        }
        event.cancelled = true
    }

    public void scanPlayers() {
        players.findAll { // игроки вне игрового региона
            it.world != center.world ||
                    it.location.distance(center) > radius
        }.each { leave it } // вылетают сразу

        players.findAll { // игроки в воде
            it.location.block.liquid
        }.each { leave it } // тоже вылетают из игры
    }

    private void increaseTimer() { // увеличиваем время на единичку
        if (!gameStarted) workspace.stopPeriod(changeTimePeriod)
        time++
        scores.time = time
    }

    private void bombing() {

        if (!players) {
            stop()
            return
        }

        if (field.stayPlatforms.size() > 1) {
            bombingPlatform field.stayPlatform
        }

        bombingDelay = workspace.addDelay({
            bombing()
        }, randomCreateBombTime)
    }

    private void bombingPlatform(HelljumpPlatform platform) {

        score += (1000 / platformCount) as int
        scores.score = score

        while (score > levelScoreLimit) {
            level++
            scores.level = level
            levelScoreLimit += basicLevelLimit + basicLevelLimit * 0.1 * (level - 1)
        }

        platform.destroy(fuseTime) {
            platform.planRegen planRegenTime
        }

    }

    public void stop() {
        if (gameStarted) {
            gameStarted = false
            workspace.stopDelay bombingDelay
            workspace.stopPeriod changeTimePeriod
            workspace.stopPeriod playerScanPeriod
            workspace.stopPeriod startCountPeriod
            workspace.stopEvent playerDamageTrigger
            workspace.stopEvent gameModeTrigger
            workspace.stopEvent leaveTrigger
            workspace.stopEvent vehicleEnterTrigger
            field.reset()
        }
        playerDatas.each {
            it.key.sendMessage "$prefix$AQUA$BOLD [GAME STOPPED]"
            it.value.restore()
        }
        playerDatas.clear()
        players.clear()
    }

    private void leave(Player player) {
        player.sendMessage(
                "$prefix$YELLOW$name$AQUA$BOLD [GAME OVER]\n" +
                        "${YELLOW}score: $AQUA$BOLD$score\n" +
                        "${YELLOW}level: $AQUA$BOLD$level\n" +
                        "${YELLOW}time: $AQUA$BOLD$time${YELLOW}s"
        )
        players.remove(player)
        playerDatas.remove(player).restore()
        endGameHandlers.each {
            try {
                it.call(player, score, level, time)
            } catch (Error ignored) {
            }
        }
        if (players.empty) stop()
    }

    public void remove() {
        if (gameStarted) stop()
        else field.reset()
        workspace.stopPeriod(playerScanPeriod)
        workspace.stopEvent(blockBreakTrigger)
        workspace.stopEvent(blockInteractTrigger)
        workspace.stopEvent(playerDamageTrigger)
        workspace.stopEvent(explodeTrigger)
        workspace.stopFinisher(finisher)
        removed = true
    }
}

public class HelljumpField {
    private static Random random = new Random()
    private final Workspace workspace
    private List<HelljumpPlatform> platforms

    public List<Block> getBlocks() {
        List<Block> blocks = []
        platforms.each { blocks += it.blocks }
        return blocks
    }

    public HelljumpField(Location center, double radius, Workspace ws) {
        this.workspace = ws
        this.platforms = findPlatforms center, radius
    }

    public void reset() {
        platforms.each { it.reset() }
    }

    public List<HelljumpPlatform> getStayPlatforms() {
        return platforms.findAll { it.stay }
    }

    public HelljumpPlatform getStayPlatform() {
        List<HelljumpPlatform> pp = getStayPlatforms()
        if (!pp) return null
        else return pp[random.nextInt(pp.size())]
    }

    public HelljumpPlatform getDeadPlatform() {
        List<HelljumpPlatform> pp = platforms.findAll { it.dead }
        if (!pp) return null
        else return pp[random.nextInt(pp.size())]
    }

    public void settlePlayers(List<Player> players) {
        players.each { player ->
            def currentPlatforms = platforms.findAll { plf ->
                !plf.center.world.entities.find {
                    it.location.distance(plf.center) < 1
                }
            }
            if (currentPlatforms) currentPlatforms = platforms
            HelljumpPlatform dest = currentPlatforms.sort { random.nextDouble() } first()
            player.teleport dest.center
        }
    }

    public List<HelljumpPlatform> getPlatforms() {
        return (List<HelljumpPlatform>) platforms.clone()
    }

    public static List<Block> getBlocksInSphere(Location center, double radius) {
        List<Block> blocks = new ArrayList<Block>()
        World world = center.world
        int xa = center.x - radius - 1, ya = center.y - radius - 1, za = center.z - radius - 1
        int xb = center.x + radius, yb = center.y + radius, zb = center.z + radius
        for (int x in xa..xb) for (int y in ya..yb) for (int z in za..zb) {
            Block block = world.getBlockAt x, y, z
            if (block.location.distance(center) < radius) blocks << block
        }
        blocks
    }

    private List<HelljumpPlatform> findPlatforms(Location center, double radius) {
        getBlocksInSphere(center, radius)
                .findAll { it.getRelative(0, 1, 0).empty } // 0 0
                .findAll { it.getRelative(0, 2, 0).empty } // 0 0
                .findAll { it.getRelative(0, -1, 0).empty } // 0 0
                .findAll { it.getRelative(1, 1, 0).empty } // 1 0
                .findAll { it.getRelative(1, 2, 0).empty } // 1 0
                .findAll { it.getRelative(1, -1, 0).empty } // 1 0
                .findAll { it.getRelative(0, 1, 1).empty } // 0 1
                .findAll { it.getRelative(0, 2, 1).empty } // 0 1
                .findAll { it.getRelative(0, -1, 1).empty } // 0 1
                .findAll { it.getRelative(1, 1, 1).empty } // 1 1
                .findAll { it.getRelative(1, 2, 1).empty } // 1 1
                .findAll { it.getRelative(1, -1, 1).empty } // 1 1
                .findAll { it.getRelative(-1, 0, -1).empty } // -1 -1
                .findAll { it.getRelative(-1, 0, 0).empty } // -1 0
                .findAll { it.getRelative(-1, 0, 1).empty } // -1 1
                .findAll { it.getRelative(-1, 0, 2).empty } // -1 2
                .findAll { it.getRelative(0, 0, -1).empty } // 0 -1
                .findAll { it.getRelative(0, 0, 2).empty } // 0 2
                .findAll { it.getRelative(1, 0, -1).empty } // 1 -1
                .findAll { it.getRelative(1, 0, 2).empty } // 1 2
                .findAll { it.getRelative(2, 0, -1).empty } // 2 -1
                .findAll { it.getRelative(2, 0, 0).empty } // 2 0
                .findAll { it.getRelative(2, 0, 1).empty } // 2 1
                .findAll { it.getRelative(2, 0, 2).empty } // 2 2
                .findAll { !it.liquid }
                .findAll { !it.getRelative(0, 0, 1).empty } // 0 1
                .findAll { !it.getRelative(1, 0, 0).empty } // 1 0
                .findAll { !it.getRelative(1, 0, 1).empty } // 1 1
                .collect {
            new HelljumpPlatform(
                    [it, it.getRelative(0, 0, 1), it.getRelative(1, 0, 0), it.getRelative(1, 0, 1)],
                    it.location.add(1, 1, 1),
                    workspace
            )
        }
    }
}

@SuppressWarnings(["GrDeprecatedAPIUsage"])
public class HelljumpPlatform {
    private final Workspace workspace
    private Collection<Block> blocks
    private List<BlockData> blockDatas
    private Location center
    private World world
    private State state = State.DEAD
    private List<Entity> entityTrash = []
    public Particle deadEffect = new Particle(name: "enchantmenttable")
    public Particle dangerEffect = new Particle(name: "flame")
    public boolean playDeadEffect = true
    public boolean playDangerEffect = false
    public boolean fadeEffect = false
    long workingTimeout = -1
    long effectPeriod = -1


    private static enum State {
        STAY, DEAD, DESTROYING, RESTORING, PLAN
    }

    public Collection<Block> getBlocks() {
        return (Collection<Block>) blocks.clone()
    }

    public HelljumpPlatform(Collection<Block> blocks, Location center, Workspace ws) {
        this.workspace = ws
        this.blocks = blocks
        this.center = center
        this.world = center.world
        this.blockDatas = blocks.collect {
            new BlockData(block: it, type: it.type, data: it.data)
        }
    }

    public boolean isStay() {
        state == State.STAY
    }

    public boolean isDead() {
        state == State.DEAD
    }

    public boolean isRestoring() {
        state == State.RESTORING
    }

    public boolean isDestroying() {
        state == State.DESTROYING
    }

    public boolean isInPlan() {
        state == State.PLAN
    }

    public Location getCenter() {
        center
    }

    public World getWorld() {
        world
    }

    public void playEffect(Particle effect, double prt) {
        double size = prt;
        int d = 1 + 10 * size;
        Location loc = center.clone().add(new Vector(-size, 0, -size))
        double dist = (2 / d) * size
        d.times { effect.play loc.add(dist, 0, 0) }
        d.times { effect.play loc.add(0, 0, dist) }
        d.times { effect.play loc.add(-dist, 0, 0) }
        d.times { effect.play loc.add(0, 0, -dist) }
    }

    public void playEffect(Particle effect) {
        int d = 10
        Location loc = center.clone().add(new Vector(-1, 0, -1))
        double dist = 2 / d
        d.times { effect.play loc.add(dist, 0, 0) }
        d.times { effect.play loc.add(0, 0, dist) }
        d.times { effect.play loc.add(-dist, 0, 0) }
        d.times { effect.play loc.add(0, 0, -dist) }
    }

    public void reset() {
        workspace.stopDelay(workingTimeout)
        workspace.stopPeriod(effectPeriod)
        state = State.STAY
        blockDatas.each {
            it.block.type = it.type
            it.block.data = it.data
        }
        entityTrash.each { it.remove() }.clear()
    }

    public void destroy(long ticks) {
        destroy(ticks, null)
    }

    public void destroy(long ticks, Closure callback) {
        if (state != State.STAY) return
        state = State.DESTROYING
        Location tntSpawnPoint = center.clone().add(0, 3, 0)
        world.playSound(center, Sound.GHAST_FIREBALL, 1, 1)
        TNTPrimed tnt = world.spawn(tntSpawnPoint, TNTPrimed)
        tnt.yield = 0
        entityTrash << tnt
        if (playDangerEffect && ticks) {
            if (fadeEffect) {
                long t = 0;
                effectPeriod = workspace.addPeriod({
                    t += 4
                    playEffect dangerEffect, t / ticks
                }, 4, 0)
            } else {
                effectPeriod = workspace.addPeriod({
                    playEffect dangerEffect
                }, 4, 0)
            }

        }
        workingTimeout = workspace.addDelay({
            workspace.stopPeriod effectPeriod
            tnt.remove()
            entityTrash.clear()
            world.createExplosion(center, 0f)
            entityTrash += blockDatas.collect {
                it.block.type = Material.AIR
                world.spawnFallingBlock(it.block.location, it.type, it.data as byte)
            }.each {
                it.velocity = new Vector(
                        it.location.x - center.x,
                        0.2,
                        it.location.z - center.z
                ).normalize().multiply(0.3)
            }
            workingTimeout = workspace.addDelay({
                entityTrash.each { it.remove() }
                state = State.DEAD
                if (callback) callback()
            }, 7)
        }, ticks)
    }

    public void regen() {
        regen(null)
    }

    public void regen(Closure callback) {
        if (state != State.DEAD && state != State.PLAN) return
        workspace.stopDelay(workingTimeout)
        workspace.stopPeriod(effectPeriod)
        world.playSound(center, Sound.CHICKEN_EGG_POP, 1f, 0.8f)
        entityTrash += blockDatas.collect {
            Location loc = it.block.location.add(0, -2, 0)
            world.spawnFallingBlock(loc, it.type, it.data as byte)
        }.each {
            it.setVelocity(new Vector(0, 0.5, 0))
        }
        workingTimeout = workspace.addDelay({
            this.reset()
            if (callback) callback()
        }, 8)
    }

    public void planRegen(long ticks) {
        planRegen(ticks, null)
    }

    public void planRegen(long ticks, Closure callback) {
        if (state != State.DEAD) return
        state = State.PLAN
        if (playDeadEffect && ticks) {
            if (fadeEffect) {
                long t = 0
                effectPeriod = workspace.addPeriod({
                    t += 4
                    playEffect deadEffect, t / ticks
                }, 4, 0)
            } else {
                effectPeriod = workspace.addPeriod({
                    playEffect deadEffect
                }, 4, 0)
            }

        }
        workingTimeout = workspace.addDelay({
            regen()
            if (callback) callback()
        }, ticks)
    }
}

public class BlockData {
    public Block block
    public Material type
    public byte data
}

@SuppressWarnings("GrDeprecatedAPIUsage")
public class PlayerData {
    public final Workspace workspace
    public final Player player
    public double hp
    public int food
    public GameMode gm
    public Location loc
    public Scoreboard scoreboard

    public PlayerData(Player player, Workspace ws) {
        this.workspace = ws
        this.player = player
        this.hp = player.health
        this.food = player.foodLevel
        this.gm = player.gameMode
        this.loc = player.location
        this.scoreboard = player.scoreboard
    }

    public void restore() {
        player.teleport loc
        player.health = hp
        player.foodLevel = food
        player.gameMode = gm
        player.fallDistance = 0
        player.activePotionEffects.each { player.removePotionEffect(it.type) }
        player.scoreboard = scoreboard
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 1))
        try {
            workspace.addDelay({ player.fireTicks = 0 }, 10)
        } catch (Exception ignored) {
        }
    }
}

public class Scores {
    private static OfflinePlayer TIME = Bukkit.getOfflinePlayer "${YELLOW}Time:"
    private static OfflinePlayer START_TIME = Bukkit.getOfflinePlayer "${YELLOW}${BOLD}Start in:"
    private static OfflinePlayer SCORE = Bukkit.getOfflinePlayer "${GREEN}${BOLD}Score:"
    private static OfflinePlayer LEVEL = Bukkit.getOfflinePlayer "${AQUA}Level:"

    private Scoreboard scoreboard
    private Objective objective

    public Scores() {
        this.scoreboard = Bukkit.scoreboardManager.newScoreboard
        this.objective = scoreboard.registerNewObjective "helljump", "${RED}[${YELLOW}Helljump${RED}]"
        this.objective.displaySlot = DisplaySlot.SIDEBAR
    }

    public void showTo(List<Player> players) {
        players.each { it.scoreboard = scoreboard }
    }

    public void reset() {
        scoreboard.resetScores TIME
        scoreboard.resetScores START_TIME
        scoreboard.resetScores SCORE
        scoreboard.resetScores LEVEL
    }

    public void setTitle(String title) {
        objective.displayName = title
    }

    public void setTime(int val) {
        objective.getScore(TIME).score = val
    }

    public void setStartTime(int val) {
        objective.getScore(START_TIME).score = val
    }

    public void setScore(int val) {
        objective.getScore(SCORE).score = val
    }

    public void setLevel(int val) {
        objective.getScore(LEVEL).score = val
    }
}

class Particle {

    RefConstructor packetConstructor = getRefClass(
            "{nms}.Packet63WorldParticles",
            "{nms}.PacketPlayOutWorldParticles",
            "{nm}.network.play.server.S2APacketParticles"
    ).findConstructor(9)

    String name = "spell"
    float dx = 0
    float dy = 0
    float dz = 0
    float speed = 0
    int count = 1

    private static Closure sendPacket;
    static {
        if (forge) {
            RefClass classPlayerMP = getRefClass "{nm}.entity.player.EntityPlayerMP"
            RefClass classPacket = getRefClass "{nm}.network.Packet"
            RefClass classNHPS = getRefClass "{nm}.network.NetHandlerPlayServer"
            RefField fieldPlayerNetServerHandler = classPlayerMP.findField classNHPS
            RefMethod methodSendPacket = classNHPS.findMethod classPacket
            sendPacket = { Player player, def packet ->
                def handler = fieldPlayerNetServerHandler.of(player.handle).get()
                methodSendPacket.of(handler).call(packet)
            }
        } else {
            sendPacket = { Player player, def packet ->
                player.handle.playerConnection.sendPacket(packet)
            }
        }
    }


    public void play(Location location) throws Exception {
        def packet = packetConstructor.create(
                name, location.x as float, location.y as float, location.z as float, dx, dy, dz, speed, count
        )
        location.world.players.findAll {
            it.location.distance(location) < 64
        }.each {
            sendPacket it, packet
        }

    }

}
