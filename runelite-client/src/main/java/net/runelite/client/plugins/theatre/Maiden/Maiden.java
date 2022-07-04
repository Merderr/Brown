package net.runelite.client.plugins.theatre.Maiden;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.ui.overlay.Overlay;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Maiden extends Room {

    @Inject
    private Client client;

    @Inject
    private MaidenOverlay maidenOverlay;

    private boolean maidenActive;

    private NPC maidenNPC;

    private List<NPC> maidenSpawns;

    private Map<NPC, Pair<Integer, Integer>> maidenReds;

    private List<WorldPoint> maidenBloodSplatters;

    private List<WorldPoint> maidenBloodSpawnLocations;

    private List<WorldPoint> maidenBloodSpawnTrailingLocations;

    private int ticksUntilAttack;

    private int lastAnimationID;

    private static final int GRAPHICSOBJECT_ID_MAIDEN = 1579;

    @Inject
    protected Maiden(TheatrePlugin plugin, TheatreConfig config) {
        super(plugin, config);
    }

    public boolean isMaidenActive() {
        return maidenActive;
    }

    public NPC getMaidenNPC() {
        return maidenNPC;
    }

    public List<NPC> getMaidenSpawns() {
        return maidenSpawns;
    }

    public Map<NPC, Pair<Integer, Integer>> getMaidenReds() {
        return maidenReds;
    }

    public List<WorldPoint> getMaidenBloodSplatters() {
        return maidenBloodSplatters;
    }

    public List<WorldPoint> getMaidenBloodSpawnLocations() {
        return maidenBloodSpawnLocations;
    }

    public List<WorldPoint> getMaidenBloodSpawnTrailingLocations() {
        return maidenBloodSpawnTrailingLocations;
    }

    public int getTicksUntilAttack() {
        return ticksUntilAttack;
    }

    public void load() {
        overlayManager.add((Overlay)maidenOverlay);
    }

    public void unload() {
        overlayManager.remove((Overlay)maidenOverlay);
        maidenActive = false;
        maidenBloodSplatters.clear();
        maidenSpawns.clear();
        maidenReds.clear();
        maidenBloodSpawnLocations.clear();
        maidenBloodSpawnTrailingLocations.clear();
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        switch (npc.getId()) {
            case 8360:
            case 8361:
            case 8362:
            case 8363:
            case 8364:
            case 8365:
            case 10814:
            case 10815:
            case 10816:
            case 10817:
            case 10818:
            case 10819:
            case 10822:
            case 10823:
            case 10824:
            case 10825:
            case 10826:
            case 10827:
                ticksUntilAttack = 10;
                maidenActive = true;
                maidenNPC = npc;
                break;
            case 8367:
            case 10821:
            case 10829:
                maidenSpawns.add(npc);
                break;
            case 8366:
            case 10820:
            case 10828:
                maidenReds.putIfAbsent(npc, new MutablePair(npc.getHealthRatio(), npc.getHealthScale()));
                break;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8360:
            case 8361:
            case 8362:
            case 8363:
            case 8364:
            case 8365:
            case 10814:
            case 10815:
            case 10816:
            case 10817:
            case 10818:
            case 10819:
            case 10822:
            case 10823:
            case 10824:
            case 10825:
            case 10826:
            case 10827:
                ticksUntilAttack = 0;
                maidenActive = false;
                maidenSpawns.clear();
                maidenNPC = null;
                break;
            case 8367:
            case 10821:
            case 10829:
                maidenSpawns.remove(npc);
                break;
            case 8366:
            case 10820:
            case 10828:
                maidenReds.remove(npc);
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!maidenActive)
            return;
        if (maidenNPC != null) {
            ticksUntilAttack--;
            if (lastAnimationID == -1 && maidenNPC.getAnimation() != lastAnimationID)
                ticksUntilAttack = 10;
            lastAnimationID = maidenNPC.getAnimation();
        }
        maidenBloodSplatters.clear();
        for (GraphicsObject graphicsObject : client.getGraphicsObjects()) {
            if (graphicsObject.getId() != 1579)
                continue;
            maidenBloodSplatters.add(WorldPoint.fromLocal(client, graphicsObject.getLocation()));
        }
        maidenBloodSpawnTrailingLocations.clear();
        maidenBloodSpawnTrailingLocations.addAll(maidenBloodSpawnLocations);
        maidenBloodSpawnLocations.clear();
        maidenSpawns.forEach(s -> maidenBloodSpawnLocations.add(s.getWorldLocation()));
    }

    Color maidenSpecialWarningColor() {
        Color col = Color.GREEN;
        if (maidenNPC == null || maidenNPC.getInteracting() == null)
            return col;
        if (maidenNPC.getInteracting().getName().equals(client.getLocalPlayer().getName()))
            return Color.ORANGE;
        return col;
    }
}
