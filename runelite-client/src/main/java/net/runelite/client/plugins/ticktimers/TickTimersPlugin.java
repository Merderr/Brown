package net.runelite.client.plugins.ticktimers;

import com.google.common.collect.UnmodifiableIterator;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
//import org.pf4j.Extension;

//@Extension
@PluginDescriptor(name = "Boss Tick Timers", enabledByDefault = false, description = "Tick timers for bosses", tags = {"pvm", "bossing"})
public class TickTimersPlugin extends Plugin {
    private static final int GENERAL_REGION = 11347;

    private static final int ARMA_REGION = 11346;

    private static final int SARA_REGION = 11602;

    private static final int ZAMMY_REGION = 11603;

    public static final int MINION_AUTO1 = 6154;

    public static final int MINION_AUTO2 = 6156;

    public static final int MINION_AUTO3 = 7071;

    public static final int MINION_AUTO4 = 7073;

    public static final int GENERAL_AUTO1 = 7018;

    public static final int GENERAL_AUTO2 = 7020;

    public static final int GENERAL_AUTO3 = 7021;

    public static final int ZAMMY_GENERIC_AUTO = 64;

    public static final int KRIL_AUTO = 6948;

    public static final int KRIL_SPEC = 6950;

    public static final int ZAKL_AUTO = 7077;

    public static final int BALFRUG_AUTO = 4630;

    public static final int ZILYANA_MELEE_AUTO = 6964;

    public static final int ZILYANA_AUTO = 6967;

    public static final int ZILYANA_SPEC = 6970;

    public static final int STARLIGHT_AUTO = 6376;

    public static final int BREE_AUTO = 7026;

    public static final int GROWLER_AUTO = 7037;

    public static final int KREE_RANGED = 6978;

    public static final int SKREE_AUTO = 6955;

    public static final int GEERIN_AUTO = 6956;

    public static final int GEERIN_FLINCH = 6958;

    public static final int KILISA_AUTO = 6957;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private TimersOverlay timersOverlay;

    @Inject
    private TickTimersConfig config;

    private Set<NPCContainer> npcContainers = new HashSet<>();

    private boolean validRegion;

    private long lastTickTime;

    Set<NPCContainer> getNpcContainers() {
        return this.npcContainers;
    }

    long getLastTickTime() {
        return this.lastTickTime;
    }

    @Provides
    TickTimersConfig getConfig(ConfigManager configManager) {
        return (TickTimersConfig)configManager.getConfig(TickTimersConfig.class);
    }

    public void startUp() {
        if (this.client.getGameState() != GameState.LOGGED_IN)
            return;
        if (regionCheck()) {
            this.npcContainers.clear();
            for (NPC npc : this.client.getNpcs())
                addNpc(npc);
            this.validRegion = true;
            this.overlayManager.add(this.timersOverlay);
        } else if (!regionCheck()) {
            this.validRegion = false;
            this.overlayManager.remove(this.timersOverlay);
            this.npcContainers.clear();
        }
    }

    public void shutDown() {
        this.npcContainers.clear();
        this.overlayManager.remove(this.timersOverlay);
        this.validRegion = false;
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
            return;
        if (regionCheck()) {
            this.npcContainers.clear();
            for (NPC npc : this.client.getNpcs())
                addNpc(npc);
            this.validRegion = true;
            this.overlayManager.add(this.timersOverlay);
        } else if (!regionCheck()) {
            this.validRegion = false;
            this.overlayManager.remove(this.timersOverlay);
            this.npcContainers.clear();
        }
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event) {
        if (!this.validRegion)
            return;
        addNpc(event.getNpc());
    }

    @Subscribe
    private void onNpcDespawned(NpcDespawned event) {
        if (!this.validRegion)
            return;
        removeNpc(event.getNpc());
    }

    @Subscribe
    public void onGameTick(GameTick Event) {
        this.lastTickTime = System.currentTimeMillis();
        if (!this.validRegion)
            return;
        handleBosses();
    }

    private void handleBosses() {
        for (NPCContainer npc : getNpcContainers()) {
            npc.setNpcInteracting(npc.getNpc().getInteracting());
            if (npc.getTicksUntilAttack() >= 0)
                npc.setTicksUntilAttack(npc.getTicksUntilAttack() - 1);
            for (UnmodifiableIterator<Integer> unmodifiableIterator = npc.getAnimations().iterator(); unmodifiableIterator.hasNext(); ) {
                int animation = ((Integer)unmodifiableIterator.next()).intValue();
                if (animation == npc.getNpc().getAnimation() && npc.getTicksUntilAttack() < 1)
                    npc.setTicksUntilAttack(npc.getAttackSpeed());
            }
        }
    }

    private boolean regionCheck() {
        return Arrays.stream(this.client.getMapRegions()).anyMatch(x ->
                (x == 11346 || x == 11347 || x == 11603 || x == 11602));
    }

    private void addNpc(NPC npc) {
        if (npc == null)
            return;
        switch (npc.getId()) {
            case 2205:
            case 2206:
            case 2207:
            case 2208:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
            case 3129:
            case 3130:
            case 3131:
            case 3132:
            case 3162:
            case 3163:
            case 3164:
            case 3165:
                if (this.config.gwd())
                    this.npcContainers.add(new NPCContainer(npc));
                break;
        }
    }

    private void removeNpc(NPC npc) {
        if (npc == null)
            return;
        switch (npc.getId()) {
            case 2205:
            case 2206:
            case 2207:
            case 2208:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
            case 3129:
            case 3130:
            case 3131:
            case 3132:
            case 3162:
            case 3163:
            case 3164:
            case 3165:
                this.npcContainers.removeIf(c -> (c.getNpc() == npc));
                break;
        }
    }
}

