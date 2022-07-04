package net.runelite.client.plugins.theatre.Bloat;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import java.awt.Color;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.ui.overlay.Overlay;

public class Bloat extends Room {
    @Inject
    private Client client;

    @Inject
    private BloatOverlay bloatOverlay;

    private boolean bloatActive;

    private NPC bloatNPC;

    private HashMap<WorldPoint, Integer> bloatHands;

    private int bloatTickCount;

    private int bloatDownCount;

    private int bloatState;

    private boolean bloatStarted;

    @Inject
    protected Bloat(Client client, TheatrePlugin plugin, TheatreConfig config) {
        super(plugin, config);
        this.bloatHands = new HashMap<>();
        this.bloatTickCount = -1;
        this.bloatDownCount = 0;
        this.bloatState = 0;
    }

    public boolean isBloatActive() {
        return this.bloatActive;
    }

    public NPC getBloatNPC() {
        return this.bloatNPC;
    }

    public HashMap<WorldPoint, Integer> getBloatHands() {
        return this.bloatHands;
    }

    public int getBloatTickCount() {
        return this.bloatTickCount;
    }

    public static final Set<Integer> tankObjectIDs = (Set<Integer>)ImmutableSet.of(Integer.valueOf(32957), Integer.valueOf(32955), Integer.valueOf(32959), Integer.valueOf(32960), Integer.valueOf(32964), Integer.valueOf(33084), new Integer[] { Integer.valueOf(0) });

    public static final Set<Integer> topOfTankObjectIDs = (Set<Integer>)ImmutableSet.of(Integer.valueOf(32958), Integer.valueOf(32962), Integer.valueOf(32964), Integer.valueOf(32965), Integer.valueOf(33062));

    public static final Set<Integer> ceilingChainsObjectIDs = (Set<Integer>)ImmutableSet.of(Integer.valueOf(32949), Integer.valueOf(32950), Integer.valueOf(32951), Integer.valueOf(32952), Integer.valueOf(32953), Integer.valueOf(32954), new Integer[] { Integer.valueOf(32970) });

    public void load() {
        this.overlayManager.add((Overlay)this.bloatOverlay);
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.bloatOverlay);
        this.bloatDownCount = 0;
        this.bloatState = 0;
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        switch (npc.getId()) {
            case 8359:
            case 10812:
            case 10813:
                this.bloatActive = true;
                this.bloatNPC = npc;
                this.bloatTickCount = 0;
                this.bloatStarted = false;
                break;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8359:
            case 10812:
            case 10813:
                this.bloatActive = false;
                this.bloatNPC = null;
                this.bloatTickCount = -1;
                break;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        if (e.getGameState() == GameState.LOGGED_IN && inRoomRegion(TheatrePlugin.BLOAT_REGION)) {
            if (this.config.hideBloatTank()) {
                removeGameObjectsFromScene(this.client.getPlane(), Ints.toArray(tankObjectIDs));
                removeGameObjectsFromScene(1, Ints.toArray(topOfTankObjectIDs));
            }
            if (this.config.hideCeilingChains())
                removeGameObjectsFromScene(1, Ints.toArray(ceilingChainsObjectIDs));
        }
    }

    //Comment out
    public void removeGameObjectsFromScene(int plane, int... gameObjectIDs) {
        /*Scene scene = this.client.getScene();
        Tile[][] tiles = scene.getTiles()[plane];
        for (int sceneTilesX = 0; sceneTilesX < 104; sceneTilesX++) {
            for (int sceneTilesY = 0; sceneTilesY < 104; sceneTilesY++) {
                Tile tile = tiles[sceneTilesX][sceneTilesY];
                if (tile != null) {
                    GameObject[] gameObjects = tile.getGameObjects();
                    Objects.requireNonNull(scene);
                    Arrays.<GameObject>stream(gameObjects).filter(Objects::nonNull).filter(gameObject -> Arrays.stream(gameObjectIDs).anyMatch(())).forEach(scene::removeGameObject);
                }
            }
        }*/
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (this.client.getGameState() != GameState.LOGGED_IN || event.getActor() != this.bloatNPC)
            return;
        this.bloatTickCount = 0;
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectC) {
        if (this.bloatActive) {
            GraphicsObject graphicsObject = graphicsObjectC.getGraphicsObject();
            if (graphicsObject.getId() >= 1560 && graphicsObject.getId() <= 1590) {
                WorldPoint point = WorldPoint.fromLocal(this.client, graphicsObject.getLocation());
                if (!this.bloatHands.containsKey(point))
                    this.bloatHands.put(point, Integer.valueOf(4));
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.bloatActive) {
            this.bloatDownCount++;
            this.bloatTickCount++;
            this.bloatHands.values().removeIf(v -> (v.intValue() <= 0));
            this.bloatHands.replaceAll((k, v) -> Integer.valueOf(v.intValue() - 1));
            if (this.bloatNPC.getAnimation() == -1) {
                this.bloatDownCount = 0;
                if (this.bloatNPC.getHealthScale() == 0) {
                    this.bloatState = 2;
                } else if (this.bloatTickCount >= 38) {
                    this.bloatState = 4;
                } else {
                    this.bloatState = 1;
                }
            } else if (this.bloatTickCount >= 38) {
                this.bloatState = 4;
            } else if (25 < this.bloatDownCount && this.bloatDownCount < 35) {
                this.bloatState = 3;
            } else if (this.bloatDownCount < 26) {
                this.bloatState = 2;
            } else if (this.bloatNPC.getModelHeight() == 568) {
                this.bloatState = 2;
            } else if (this.bloatTickCount >= 38) {
                this.bloatState = 4;
            } else {
                this.bloatState = 1;
            }
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (this.client.getVarbitValue(6447) == 1 && !this.bloatStarted) {
            this.bloatTickCount = 0;
            this.bloatStarted = true;
        }
    }

    Polygon getBloatTilePoly() {
        LocalPoint lp;
        if (this.bloatNPC == null)
            return null;
        int size = 1;
        NPCComposition composition = this.bloatNPC.getTransformedComposition();
        if (composition != null)
            size = composition.getSize();
        switch (this.bloatState) {
            case 1:
            case 4:
                lp = this.bloatNPC.getLocalLocation();
                if (lp == null)
                    return null;
                return RoomOverlay.getCanvasTileAreaPoly(this.client, lp, size, true);
            case 2:
            case 3:
                lp = LocalPoint.fromWorld(this.client, this.bloatNPC.getWorldLocation());
                if (lp == null)
                    return null;
                return RoomOverlay.getCanvasTileAreaPoly(this.client, lp, size, false);
        }
        return null;
    }

    Color getBloatStateColor() {
        Color col = this.config.bloatIndicatorColorUP();
        switch (this.bloatState) {
            case 2:
                col = this.config.bloatIndicatorColorDOWN();
                break;
            case 3:
                col = this.config.bloatIndicatorColorWARN();
                break;
            case 4:
                col = this.config.bloatIndicatorColorTHRESH();
                break;
        }
        return col;
    }

    public int getBloatDownCount() {
        return this.bloatDownCount;
    }

    public int getBloatState() {
        return this.bloatState;
    }
}
