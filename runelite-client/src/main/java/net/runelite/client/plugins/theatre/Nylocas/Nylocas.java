package net.runelite.client.plugins.theatre.Nylocas;

import com.google.common.collect.ImmutableSet;
import net.runelite.client.plugins.spoontob.util.WeaponMap;
import net.runelite.client.plugins.spoontob.util.WeaponStyle;
import net.runelite.api.events.MenuOptionClicked;
import java.awt.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.util.Text;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.theatre.Room;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatreInputListener;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.lang3.ObjectUtils;

public class Nylocas extends Room {
    @Inject
    private Client client;

    @Inject
    private NylocasOverlay nylocasOverlay;

    @Inject
    private NylocasAliveCounterOverlay nylocasAliveCounterOverlay;

    @Inject
    private TheatreInputListener theatreInputListener;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private SkillIconManager skillIconManager;

    private boolean nyloActive;

    private NPC nyloBossNPC;

    private boolean nyloBossAlive;

    @Inject
    protected Nylocas(TheatrePlugin plugin, TheatreConfig config) {
        super(plugin, config);
        this.nylocasPillars = new HashMap<>();
        this.nylocasNpcs = new HashMap<>();
        this.aggressiveNylocas = new HashSet<>();
        this.instanceTimer = 0;
        this.isInstanceTimerRunning = false;
        this.nyloBossAttackTickCount = -1;
        this.nyloBossSwitchTickCount = -1;
        this.nyloBossTotalTickCount = -1;
        this.nyloBossStage = 0;
        this.currentWave = new HashMap<>();
        this.splitsMap = new HashMap<>();
        this.bigNylos = new HashSet<>();
        this.varbit6447 = -1;
        this.nextInstance = true;
        this.nyloWave = 0;
        this.ticksUntilNextWave = 0;
        this.ticksSinceLastWave = 0;
        this.totalStalledWaves = 0;
        this.skipTickCheck = false;
    }

    public boolean isNyloActive() {
        return this.nyloActive;
    }

    public NPC getNyloBossNPC() {
        return this.nyloBossNPC;
    }

    public boolean isNyloBossAlive() {
        return this.nyloBossAlive;
    }

    public static void setWave31Callback(Runnable wave31Callback) {
        Nylocas.wave31Callback = wave31Callback;
    }

    public static Runnable getWave31Callback() {
        return wave31Callback;
    }

    private static Runnable wave31Callback = null;

    public static void setEndOfWavesCallback(Runnable endOfWavesCallback) {
        Nylocas.endOfWavesCallback = endOfWavesCallback;
    }

    public static Runnable getEndOfWavesCallback() {
        return endOfWavesCallback;
    }

    private static Runnable endOfWavesCallback = null;

    private HashMap<NPC, Integer> nylocasPillars;

    private HashMap<NPC, Integer> nylocasNpcs;

    private HashSet<NPC> aggressiveNylocas;

    private Instant nyloWaveStart;

    private int instanceTimer;

    private boolean isInstanceTimerRunning;

    private NyloSelectionManager nyloSelectionManager;

    private int nyloBossAttackTickCount;

    private int nyloBossSwitchTickCount;

    private int nyloBossTotalTickCount;

    private int nyloBossStage;

    private WeaponStyle weaponStyle;

    private HashMap<NyloNPC, NPC> currentWave;

    private final Map<LocalPoint, Integer> splitsMap;

    private final Set<NPC> bigNylos;

    private int varbit6447;

    private boolean nextInstance;

    private int nyloWave;

    private int ticksUntilNextWave;

    private int ticksSinceLastWave;

    private int totalStalledWaves;

    private static final int NPCID_NYLOCAS_PILLAR = 8358;

    private boolean skipTickCheck;

    private static final String MAGE_NYLO = "Nylocas Hagios";

    private static final String RANGE_NYLO = "Nylocas Toxobolos";

    private static final String MELEE_NYLO = "Nylocas Ischyros";

    public HashMap<NPC, Integer> getNylocasPillars() {
        return this.nylocasPillars;
    }

    public HashMap<NPC, Integer> getNylocasNpcs() {
        return this.nylocasNpcs;
    }

    public HashSet<NPC> getAggressiveNylocas() {
        return this.aggressiveNylocas;
    }

    public Instant getNyloWaveStart() {
        return this.nyloWaveStart;
    }

    public int getInstanceTimer() {
        return this.instanceTimer;
    }

    public boolean isInstanceTimerRunning() {
        return this.isInstanceTimerRunning;
    }

    public NyloSelectionManager getNyloSelectionManager() {
        return this.nyloSelectionManager;
    }

    public int getNyloBossAttackTickCount() {
        return this.nyloBossAttackTickCount;
    }

    public int getNyloBossSwitchTickCount() {
        return this.nyloBossSwitchTickCount;
    }

    public int getNyloBossTotalTickCount() {
        return this.nyloBossTotalTickCount;
    }

    public int getNyloBossStage() {
        return this.nyloBossStage;
    }

    public Map<LocalPoint, Integer> getSplitsMap() {
        return this.splitsMap;
    }

    public int getNyloWave() {
        return this.nyloWave;
    }

    public int getTicksUntilNextWave() {
        return this.ticksUntilNextWave;
    }

    private static final Set<Integer> NYLO_BOSS_MELEE = ImmutableSet.of(10808, 10804, 8355);

    private static final Set<Integer> NYLO_BOSS_MAGE = ImmutableSet.of(Integer.valueOf(10809), Integer.valueOf(10805), Integer.valueOf(8356));

    private static final Set<Integer> NYLO_BOSS_RANGE = ImmutableSet.of(Integer.valueOf(10810), Integer.valueOf(10806), Integer.valueOf(8357));

    public void init() {
        InfoBoxComponent box = new InfoBoxComponent();
        box.setImage(this.skillIconManager.getSkillImage(Skill.ATTACK));
        NyloSelectionBox nyloMeleeOverlay = new NyloSelectionBox(box);
        nyloMeleeOverlay.setSelected(this.config.getHighlightMeleeNylo());
        box = new InfoBoxComponent();
        box.setImage(this.skillIconManager.getSkillImage(Skill.MAGIC));
        NyloSelectionBox nyloMageOverlay = new NyloSelectionBox(box);
        nyloMageOverlay.setSelected(this.config.getHighlightMageNylo());
        box = new InfoBoxComponent();
        box.setImage(this.skillIconManager.getSkillImage(Skill.RANGED));
        NyloSelectionBox nyloRangeOverlay = new NyloSelectionBox(box);
        nyloRangeOverlay.setSelected(this.config.getHighlightRangeNylo());
        this.nyloSelectionManager = new NyloSelectionManager(nyloMeleeOverlay, nyloMageOverlay, nyloRangeOverlay);
        this.nyloSelectionManager.setHidden(!this.config.nyloHighlightOverlay());
        this.nylocasAliveCounterOverlay.setHidden(!this.config.nyloAlivePanel());
        this.nylocasAliveCounterOverlay.setNyloAlive(0);
        this.nylocasAliveCounterOverlay.setMaxNyloAlive(12);
        this.nyloBossNPC = null;
        this.nyloBossAlive = false;
    }

    private void startupNyloOverlay() {
        this.mouseManager.registerMouseListener((MouseListener)this.theatreInputListener);
        if (this.nyloSelectionManager != null) {
            this.overlayManager.add(this.nyloSelectionManager);
            this.nyloSelectionManager.setHidden(!this.config.nyloHighlightOverlay());
        }
        if (this.nylocasAliveCounterOverlay != null) {
            this.overlayManager.add(this.nylocasAliveCounterOverlay);
            this.nylocasAliveCounterOverlay.setHidden(!this.config.nyloAlivePanel());
        }
    }

    private void shutdownNyloOverlay() {
        this.mouseManager.unregisterMouseListener((MouseListener)this.theatreInputListener);
        if (this.nyloSelectionManager != null) {
            this.overlayManager.remove(this.nyloSelectionManager);
            this.nyloSelectionManager.setHidden(true);
        }
        if (this.nylocasAliveCounterOverlay != null) {
            this.overlayManager.remove(this.nylocasAliveCounterOverlay);
            this.nylocasAliveCounterOverlay.setHidden(true);
        }
    }

    public void load() {
        this.overlayManager.add((Overlay)this.nylocasOverlay);
        this.weaponStyle = null;
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.nylocasOverlay);
        shutdownNyloOverlay();
        this.nyloBossNPC = null;
        this.nyloBossAlive = false;
        this.nyloWaveStart = null;
        this.weaponStyle = null;
        this.splitsMap.clear();
        this.bigNylos.clear();
    }

    private void resetNylo() {
        this.nyloBossNPC = null;
        this.nyloBossAlive = false;
        this.nylocasPillars.clear();
        this.nylocasNpcs.clear();
        this.aggressiveNylocas.clear();
        setNyloWave(0);
        this.currentWave.clear();
        this.totalStalledWaves = 0;
        this.weaponStyle = null;
        this.splitsMap.clear();
        this.bigNylos.clear();
    }

    private void setNyloWave(int wave) {
        /*this.nyloWave = wave;
        this.nylocasAliveCounterOverlay.setWave(wave);
        if (wave >= 3)
            this.isInstanceTimerRunning = false;
        if (wave != 0) {
            this.ticksSinceLastWave = ((NylocasWave)NylocasWave.waves.get(Integer.valueOf(wave))).getWaveDelay();
            this.ticksUntilNextWave = ((NylocasWave)NylocasWave.waves.get(Integer.valueOf(wave))).getWaveDelay();
        }
        if (wave >= 20)
            if (this.nylocasAliveCounterOverlay.getMaxNyloAlive() != 24)
                this.nylocasAliveCounterOverlay.setMaxNyloAlive(24);
        if (wave < 20)
            if (this.nylocasAliveCounterOverlay.getMaxNyloAlive() != 12)
                this.nylocasAliveCounterOverlay.setMaxNyloAlive(12);
        if (wave == 31 && wave31Callback != null)
            wave31Callback.run();
        */
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged change) {
        if (change.getKey().equals("nyloHighlightOverlay"))
            this.nyloSelectionManager.setHidden(!this.config.nyloHighlightOverlay());
        if (change.getKey().equals("nyloAliveCounter"))
            this.nylocasAliveCounterOverlay.setHidden(!this.config.nyloAlivePanel());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        switch (npc.getId()) {
            case 8358:
            case 10790:
            case 10811:
                this.nyloActive = true;
                if (this.nylocasPillars.size() > 3)
                    this.nylocasPillars.clear();
                if (!this.nylocasPillars.containsKey(npc))
                    this.nylocasPillars.put(npc, Integer.valueOf(100));
                break;
            case 8342:
            case 8343:
            case 8344:
            case 8345:
            case 8346:
            case 8347:
            case 8348:
            case 8349:
            case 8350:
            case 8351:
            case 8352:
            case 8353:
            case 10774:
            case 10775:
            case 10776:
            case 10777:
            case 10778:
            case 10779:
            case 10780:
            case 10781:
            case 10782:
            case 10783:
            case 10784:
            case 10785:
            case 10791:
            case 10792:
            case 10793:
            case 10794:
            case 10795:
            case 10796:
            case 10797:
            case 10798:
            case 10799:
            case 10800:
            case 10801:
            case 10802:
                if (this.nyloActive) {
                    this.nylocasNpcs.put(npc, Integer.valueOf(52));
                    this.nylocasAliveCounterOverlay.setNyloAlive(this.nylocasNpcs.size());
                    NyloNPC nyloNPC = matchNpc(npc);
                    if (nyloNPC != null) {
                        this.currentWave.put(nyloNPC, npc);
                        if (this.currentWave.size() > 2)
                            matchWave();
                    }
                }
                break;
            case 8354:
            case 8355:
            case 8356:
            case 8357:
            case 10786:
            case 10787:
            case 10788:
            case 10789:
            case 10807:
            case 10808:
            case 10809:
            case 10810:
                this.nyloBossTotalTickCount = -4;
                this.nyloBossAlive = true;
                this.isInstanceTimerRunning = false;
                this.nyloBossNPC = npc;
                break;
        }
        int id = npc.getId();
        switch (id) {
            case 8345:
            case 8346:
            case 8347:
            case 10794:
            case 10795:
            case 10796:
                this.bigNylos.add(npc);
                break;
        }
    }

    private void matchWave() {
        /*
        Set<NyloNPC> currentWaveKeySet = this.currentWave.keySet();
        for (int wave = this.nyloWave + 1; wave <= 31; wave++) {
            boolean matched = true;
            HashSet<NyloNPC> potentialWave = ((NylocasWave)NylocasWave.waves.get(Integer.valueOf(wave))).getWaveData();
            for (NyloNPC nyloNpc : potentialWave) {
                if (!currentWaveKeySet.contains(nyloNpc)) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                setNyloWave(wave);
                for (NyloNPC nyloNPC : potentialWave) {
                    if (nyloNPC.isAggressive())
                        this.aggressiveNylocas.add(this.currentWave.get(nyloNPC));
                }
                this.currentWave.clear();
                return;
            }
        }*/
    }

    private NyloNPC matchNpc(NPC npc) {
        WorldPoint p = WorldPoint.fromLocalInstance(this.client, npc.getLocalLocation());
        Point point = new Point(p.getRegionX(), p.getRegionY());
        NylocasSpawnPoint spawnPoint = NylocasSpawnPoint.getLookupMap().get(point);
        if (spawnPoint == null)
            return null;
        NylocasType nylocasType = NylocasType.getLookupMap().get(Integer.valueOf(npc.getId()));
        if (nylocasType == null)
            return null;
        return new NyloNPC(nylocasType, spawnPoint);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8358:
            case 10790:
            case 10811:
                if (this.nylocasPillars.containsKey(npc))
                    this.nylocasPillars.remove(npc);
                if (this.nylocasPillars.size() < 1) {
                    this.nyloWaveStart = null;
                    this.nyloActive = false;
                }
                break;
            case 8342:
            case 8343:
            case 8344:
            case 8345:
            case 8346:
            case 8347:
            case 8348:
            case 8349:
            case 8350:
            case 8351:
            case 8352:
            case 8353:
            case 10774:
            case 10775:
            case 10776:
            case 10777:
            case 10778:
            case 10779:
            case 10780:
            case 10781:
            case 10782:
            case 10783:
            case 10784:
            case 10785:
            case 10791:
            case 10792:
            case 10793:
            case 10794:
            case 10795:
            case 10796:
            case 10797:
            case 10798:
            case 10799:
            case 10800:
            case 10801:
            case 10802:
                if (this.nylocasNpcs.remove(npc) != null)
                    this.nylocasAliveCounterOverlay.setNyloAlive(this.nylocasNpcs.size());
                this.aggressiveNylocas.remove(npc);
                if (this.nyloWave == 31 && this.nylocasNpcs.size() == 0 && endOfWavesCallback != null)
                    endOfWavesCallback.run();
                break;
            case 8354:
            case 8355:
            case 8356:
            case 8357:
            case 10786:
            case 10787:
            case 10788:
            case 10789:
            case 10807:
            case 10808:
            case 10809:
            case 10810:
                this.nyloBossAlive = false;
                this.nyloBossAttackTickCount = -1;
                this.nyloBossSwitchTickCount = -1;
                this.nyloBossTotalTickCount = -1;
                break;
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        Actor actor = event.getActor();
        if (actor instanceof NPC)
            switch (((NPC)actor).getId()) {
                case 8355:
                case 8356:
                case 8357:
                case 10787:
                case 10788:
                case 10789:
                case 10808:
                case 10809:
                case 10810:
                    if (event.getActor().getAnimation() == 8004 || event
                            .getActor().getAnimation() == 7999 || event
                            .getActor().getAnimation() == 7989) {
                        this.nyloBossAttackTickCount = 5;
                        this.nyloBossStage++;
                    }
                    break;
            }
        if (!this.bigNylos.isEmpty() && event.getActor() instanceof NPC) {
            NPC npc = (NPC)event.getActor();
            if (this.bigNylos.contains(npc)) {
                int anim = npc.getAnimation();
                if (anim == 8005 || anim == 7991 || anim == 7998) {
                    this.splitsMap.putIfAbsent(npc.getLocalLocation(), Integer.valueOf(6));
                    this.bigNylos.remove(npc);
                }
            }
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged npcChanged) {
        int npcId = npcChanged.getNpc().getId();
        switch (npcId) {
            case 8355:
            case 8356:
            case 8357:
            case 10787:
            case 10788:
            case 10789:
            case 10808:
            case 10809:
            case 10810:
                this.nyloBossAttackTickCount = 3;
                this.nyloBossSwitchTickCount = 11;
                this.nyloBossStage = 0;
                break;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        int[] varps = this.client.getVarps();
        int newVarbit6447 = this.client.getVarbitValue(varps, 6447);
        if (inRoomRegion(TheatrePlugin.NYLOCAS_REGION) && newVarbit6447 != 0 && newVarbit6447 != this.varbit6447) {
            this.nyloWaveStart = Instant.now();
            if (this.nylocasAliveCounterOverlay != null)
                this.nylocasAliveCounterOverlay.setNyloWaveStart(this.nyloWaveStart);
        }
        this.varbit6447 = newVarbit6447;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() != GameState.LOGGED_IN)
            return;
        if (inRoomRegion(TheatrePlugin.NYLOCAS_REGION)) {
            startupNyloOverlay();
        } else {
            if (!this.nyloSelectionManager.isHidden() || !this.nylocasAliveCounterOverlay.isHidden())
                shutdownNyloOverlay();
            resetNylo();
            this.isInstanceTimerRunning = false;
        }
        this.nextInstance = true;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (inRoomRegion(TheatrePlugin.NYLOCAS_REGION) && this.nyloActive) {
            if (this.skipTickCheck) {
                this.skipTickCheck = false;
            } else {
                if (this.client.getLocalPlayer() == null || this.client.getLocalPlayer().getPlayerComposition() == null)
                    return;
                int equippedWeapon = ((Integer)ObjectUtils.defaultIfNull(Integer.valueOf(this.client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON)), Integer.valueOf(-1))).intValue();
                this.weaponStyle = (WeaponStyle)WeaponMap.StyleMap.get(Integer.valueOf(equippedWeapon));
            }
            Iterator<NPC> it;
            for (it = this.nylocasNpcs.keySet().iterator(); it.hasNext(); ) {
                NPC npc = it.next();
                int ticksLeft = ((Integer)this.nylocasNpcs.get(npc)).intValue();
                if (ticksLeft < 0) {
                    it.remove();
                    continue;
                }
                this.nylocasNpcs.replace(npc, Integer.valueOf(ticksLeft - 1));
            }
            for (it = this.nylocasPillars.keySet().iterator(); it.hasNext(); ) {
                NPC pillar = it.next();
                int healthPercent = pillar.getHealthRatio();
                if (healthPercent > -1)
                    this.nylocasPillars.replace(pillar, Integer.valueOf(healthPercent));
            }
            if ((this.instanceTimer + 1) % 4 == 1 && this.nyloWave < 31 && this.ticksSinceLastWave < 2) {
                if (this.config.nyloStallMessage() && this.nylocasAliveCounterOverlay.getNyloAlive() >= this.nylocasAliveCounterOverlay.getMaxNyloAlive()) {
                    this.totalStalledWaves++;
                    this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Stalled Wave: <col=EF1020>" + this.nyloWave + "/31<col=00> - Time:<col=EF1020> " + this.nylocasAliveCounterOverlay
                            .getFormattedTime() + " <col=00>- Nylos Alive: <col=EF1020>" + this.nylocasAliveCounterOverlay
                            .getNyloAlive() + "/" + this.nylocasAliveCounterOverlay.getMaxNyloAlive() + " <col=00>- Total Stalled Waves: <col=EF1020>" + this.totalStalledWaves, "");
                }
                this.ticksUntilNextWave = 4;
            }
            this.ticksSinceLastWave = Math.max(0, this.ticksSinceLastWave - 1);
            this.ticksUntilNextWave = Math.max(0, this.ticksUntilNextWave - 1);
            if (!this.splitsMap.isEmpty()) {
                this.splitsMap.values().removeIf(value -> (value.intValue() <= 1));
                this.splitsMap.replaceAll((key, value) -> Integer.valueOf(value.intValue() - 1));
            }
        }
        if (this.nyloActive && this.nyloBossAlive) {
            this.nyloBossAttackTickCount--;
            this.nyloBossSwitchTickCount--;
            this.nyloBossTotalTickCount++;
        }
        this.instanceTimer = (this.instanceTimer + 1) % 4;
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        List<Player> players = this.client.getPlayers();
        for (Player player : players) {
            if (player.getWorldLocation() != null) {
                LocalPoint lp = player.getLocalLocation();
                WorldPoint wp = WorldPoint.fromRegion(player.getWorldLocation().getRegionID(), 5, 33, 0);
                LocalPoint lp1 = LocalPoint.fromWorld(this.client, wp.getX(), wp.getY());
                if (lp1 != null) {
                    Point base = new Point(lp1.getSceneX(), lp1.getSceneY());
                    Point point = new Point(lp.getSceneX() - base.getX(), lp.getSceneY() - base.getY());
                    if (inRoomRegion(TheatrePlugin.BLOAT_REGION) && point.getX() == -1 && (point.getY() == -1 || point.getY() == -2 || point.getY() == -3) && this.nextInstance) {
                        this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Nylo instance timer started.", "");
                        this.instanceTimer = 3;
                        this.isInstanceTimerRunning = true;
                        this.nextInstance = false;
                    }
                }
            }
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.isItemOp() && event.getItemOp() == 2) {
            WeaponStyle newStyle = (WeaponStyle)WeaponMap.StyleMap.get(Integer.valueOf(event.getItemId()));
            if (newStyle != null) {
                this.skipTickCheck = true;
                this.weaponStyle = newStyle;
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded entry) {
        if (this.nyloActive || this.nyloBossAlive)
            if (this.config.removeNyloBossEntries() && entry.getMenuAction() == MenuAction.NPC_SECOND_OPTION && this.weaponStyle != null) {
                NPC npc = this.client.getCachedNPCs()[entry.getIdentifier()];
                if (npc != null) {
                    int id = npc.getId();
                    switch (this.weaponStyle) {
                        case MAGIC:
                            if (NYLO_BOSS_MELEE.contains(Integer.valueOf(id)) || NYLO_BOSS_RANGE.contains(Integer.valueOf(id)))
                                this.client.setMenuOptionCount(this.client.getMenuOptionCount() - 1);
                            break;
                        case MELEE:
                            if (NYLO_BOSS_RANGE.contains(Integer.valueOf(id)) || NYLO_BOSS_MAGE.contains(Integer.valueOf(id)))
                                this.client.setMenuOptionCount(this.client.getMenuOptionCount() - 1);
                            break;
                        case RANGE:
                            if (NYLO_BOSS_MELEE.contains(Integer.valueOf(id)) || NYLO_BOSS_MAGE.contains(Integer.valueOf(id)))
                                this.client.setMenuOptionCount(this.client.getMenuOptionCount() - 1);
                            break;
                    }
                }
            }
        if (!this.nyloActive)
            return;
        String target = entry.getTarget();
        if (this.config.removeNyloEntries() && entry.getMenuAction() == MenuAction.NPC_SECOND_OPTION && this.weaponStyle != null)
            switch (this.weaponStyle) {
                case MAGIC:
                    if (target.contains("Nylocas Ischyros") || target.contains("Nylocas Toxobolos"))
                        this.client.setMenuOptionCount(this.client.getMenuOptionCount() - 1);
                    break;
                case MELEE:
                    if (target.contains("Nylocas Toxobolos") || target.contains("Nylocas Hagios"))
                        this.client.setMenuOptionCount(this.client.getMenuOptionCount() - 1);
                    break;
                case RANGE:
                    if (target.contains("Nylocas Ischyros") || target.contains("Nylocas Hagios"))
                        this.client.setMenuOptionCount(this.client.getMenuOptionCount() - 1);
                    break;
            }
        if (this.config.nyloRecolorMenu() && entry.getOption().equals("Attack")) {
            MenuEntry[] entries = this.client.getMenuEntries();
            MenuEntry toEdit = entries[entries.length - 1];
            String strippedTarget = Text.removeTags(target);
            if (strippedTarget.startsWith("Nylocas Hagios")) {
                toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, Color.CYAN));
            } else if (strippedTarget.startsWith("Nylocas Ischyros")) {
                toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(255, 188, 188)));
            } else if (strippedTarget.startsWith("Nylocas Toxobolos")) {
                toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, Color.GREEN));
            }
            this.client.setMenuEntries(entries);
        }
    }

    @Subscribe
    public void onMenuOpened(MenuOpened menu) {
        if (!this.config.nyloRecolorMenu() || !this.nyloActive || !this.nyloBossAlive)
            return;
        this.client.setMenuEntries((MenuEntry[])Arrays.<MenuEntry>stream(menu.getMenuEntries()).filter(s -> !s.getOption().equals("Examine")).toArray(x$0 -> new MenuEntry[x$0]));
    }
}
