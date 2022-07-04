package net.runelite.client.plugins.theatre.Sotetseg;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.plugins.theatre.prayer.TheatrePrayerUtil;
import net.runelite.client.plugins.theatre.prayer.TheatreUpcomingAttack;
import net.runelite.client.ui.overlay.Overlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sotetseg extends Room {
    private static final Logger log = LoggerFactory.getLogger(Sotetseg.class);

    @Inject
    private Client client;

    @Inject
    private TheatrePlugin plugin;

    @Inject
    private SotetsegOverlay sotetsegOverlay;

    @Inject
    private SotetsegPrayerOverlay sotetsegPrayerOverlay;

    private boolean sotetsegActive;

    private NPC sotetsegNPC;

    private LinkedHashSet<Point> redTiles;

    private HashSet<Point> greenTiles;

    @Inject
    protected Sotetseg(TheatrePlugin plugin, TheatreConfig config) {
        super(plugin, config);
        this.redTiles = new LinkedHashSet<>();
        this.greenTiles = new HashSet<>();
        this.wasInUnderWorld = false;
        this.sotetsegTickCount = -1;
        this.offTick = false;
        this.bigOrbPresent = false;
        this.sotetsegBallCounted = false;
        this.overWorldRegionID = -1;
        this.upcomingAttackQueue = new PriorityQueue<>();
        this.attacksLeft = 10;
    }

    public boolean isSotetsegActive() {
        return this.sotetsegActive;
    }

    public NPC getSotetsegNPC() {
        return this.sotetsegNPC;
    }

    public LinkedHashSet<Point> getRedTiles() {
        return this.redTiles;
    }

    public HashSet<Point> getGreenTiles() {
        return this.greenTiles;
    }

    public static Point getSwMazeSquareOverWorld() {
        return swMazeSquareOverWorld;
    }

    private static final Point swMazeSquareOverWorld = new Point(9, 22);

    public static Point getSwMazeSquareUnderWorld() {
        return swMazeSquareUnderWorld;
    }

    private static final Point swMazeSquareUnderWorld = new Point(42, 31);

    private boolean wasInUnderWorld;

    private int sotetsegTickCount;

    private boolean offTick;

    private boolean bigOrbPresent;

    private boolean sotetsegBallCounted;

    static final int SOTETSEG_MAGE_ORB = 1606;

    static final int SOTETSEG_RANGE_ORB = 1607;

    static final int SOTETSEG_BIG_AOE_ORB = 1604;

    private static final int GROUNDOBJECT_ID_REDMAZE = 33035;

    private int overWorldRegionID;

    private long lastTick;

    Queue<TheatreUpcomingAttack> upcomingAttackQueue;

    private int attacksLeft;

    public boolean isWasInUnderWorld() {
        return this.wasInUnderWorld;
    }

    public int getSotetsegTickCount() {
        return this.sotetsegTickCount;
    }

    long getLastTick() {
        return this.lastTick;
    }

    Queue<TheatreUpcomingAttack> getUpcomingAttackQueue() {
        return this.upcomingAttackQueue;
    }

    public int getAttacksLeft() {
        return this.attacksLeft;
    }

    public void load() {
        this.overlayManager.add((Overlay)this.sotetsegOverlay);
        this.overlayManager.add((Overlay)this.sotetsegPrayerOverlay);
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.sotetsegOverlay);
        this.overlayManager.remove((Overlay)this.sotetsegPrayerOverlay);
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
            case 10864:
            case 10865:
            case 10867:
            case 10868:
                this.sotetsegActive = true;
                this.sotetsegNPC = npc;
                break;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
            case 10864:
            case 10865:
            case 10867:
            case 10868:
                if (this.client.getPlane() != 3) {
                    this.sotetsegActive = false;
                    this.sotetsegNPC = null;
                    this.upcomingAttackQueue.clear();
                    this.attacksLeft = 10;
                }
                break;
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        Actor actor = event.getActor();
        if (actor instanceof NPC)
            if (actor == this.sotetsegNPC) {
                int animation = event.getActor().getAnimation();
                switch (animation) {
                    case 8138:
                    case 8139:
                        this.sotetsegTickCount = 6;
                        this.upcomingAttackQueue.add(new TheatreUpcomingAttack(this.sotetsegTickCount, Prayer.PROTECT_FROM_MELEE, 1));
                        break;
                }
            }
    }

    @Subscribe
    public void onProjectileSpawned(ProjectileSpawned projectileSpawned) {
        if (!this.sotetsegActive)
            return;
        Projectile p = projectileSpawned.getProjectile();
        if (p == null)
            return;
        if (p.getInteracting() == this.client.getLocalPlayer() && (p.getId() == 1606 || p.getId() == 1607))
            this.upcomingAttackQueue.add(new TheatreUpcomingAttack(p
                    .getRemainingCycles() / 30,
                    (p.getId() == 1606) ? Prayer.PROTECT_FROM_MAGIC : Prayer.PROTECT_FROM_MISSILES));
        if (p.getId() == 1604) {
            this.sotetsegTickCount = 11;
            this.attacksLeft = 10;
        }
        WorldPoint soteWp = WorldPoint.fromLocal(this.client, this.sotetsegNPC.getLocalLocation());
        WorldPoint projWp = WorldPoint.fromLocal(this.client, p.getX1(), p.getY1(), this.client.getPlane());
        if (p.getId() == 1606 && this.sotetsegNPC.getAnimation() == 8139 && projWp.equals(soteWp))
            this.attacksLeft--;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.sotetsegActive) {
            this.lastTick = System.currentTimeMillis();
            TheatrePrayerUtil.updateNextPrayerQueue(getUpcomingAttackQueue());
            if (this.sotetsegNPC != null && (this.sotetsegNPC.getId() == 8388 || this.sotetsegNPC.getId() == 10865 || this.sotetsegNPC.getId() == 10868)) {
                if (this.sotetsegTickCount >= 0)
                    this.sotetsegTickCount--;
                if (!this.redTiles.isEmpty()) {
                    this.redTiles.clear();
                    this.offTick = false;
                }
                if (!this.greenTiles.isEmpty())
                    this.greenTiles.clear();
                if (inRoomRegion(TheatrePlugin.SOTETSEG_REGION_OVERWORLD)) {
                    this.wasInUnderWorld = false;
                    if (this.client.getLocalPlayer() != null && this.client.getLocalPlayer().getWorldLocation() != null)
                        this.overWorldRegionID = this.client.getLocalPlayer().getWorldLocation().getRegionID();
                }
            }
            if (this.config.sotetsegBigOrbTicks()) {
                boolean foundBigOrb = false;
                for (Projectile p : this.client.getProjectiles()) {
                    if (p.getId() == 1604) {
                        foundBigOrb = true;
                        break;
                    }
                }
                this.bigOrbPresent = foundBigOrb;
            }
            if (!this.bigOrbPresent)
                this.sotetsegBallCounted = false;
            if (this.bigOrbPresent && !this.sotetsegBallCounted) {
                this.sotetsegTickCount = 10;
                this.sotetsegBallCounted = true;
            }
        }
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        if (this.sotetsegActive) {
            GroundObject o = event.getGroundObject();
            if (o.getId() == 33035) {
                Tile t = event.getTile();
                WorldPoint p = WorldPoint.fromLocal(this.client, t.getLocalLocation());
                Point point = new Point(p.getRegionX(), p.getRegionY());
                if (inRoomRegion(TheatrePlugin.SOTETSEG_REGION_OVERWORLD))
                    this.redTiles.add(new Point(point.getX() - swMazeSquareOverWorld.getX(), point.getY() - swMazeSquareOverWorld.getY()));
                if (inRoomRegion(TheatrePlugin.SOTETSEG_REGION_UNDERWORLD)) {
                    this.redTiles.add(new Point(point.getX() - swMazeSquareUnderWorld.getX(), point.getY() - swMazeSquareUnderWorld.getY()));
                    this.wasInUnderWorld = true;
                }
            }
        }
    }

    WorldPoint worldPointFromMazePoint(Point mazePoint) {
        if (this.overWorldRegionID == -1 && this.client.getLocalPlayer() != null)
            return WorldPoint.fromRegion(this.client
                    .getLocalPlayer().getWorldLocation().getRegionID(), mazePoint.getX() + getSwMazeSquareOverWorld().getX(), mazePoint
                    .getY() + getSwMazeSquareOverWorld().getY(), 0);
        return WorldPoint.fromRegion(this.overWorldRegionID, mazePoint
                .getX() + getSwMazeSquareOverWorld().getX(), mazePoint
                .getY() + getSwMazeSquareOverWorld().getY(), 0);
    }
}
