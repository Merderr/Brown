package net.runelite.client.plugins.spoontobstats;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.Text;

@PluginDescriptor(name = "<html><font color=#25c550>[S] Theatre Stats", description = "Theatre of Blood room splits and damage", tags = {"combat", "raid", "pve", "pvm", "bosses", "timer"}, enabledByDefault = true, conflicts = {"Theatre of Blood Stats"})
public class SpoonTobStatsPlugin extends Plugin {
    private static final DecimalFormat DMG_FORMAT = new DecimalFormat("#,##0");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0");

    private static final int THEATRE_OF_BLOOD_ROOM_STATUS = 6447;

    private static final int THEATRE_OF_BLOOD_BOSS_HP = 6448;

    private static final int TOB_LOBBY = 14642;

    private static final int MAIDEN_REGION = 12613;

    private static final int BLOAT_REGION = 13125;

    private static final int NYLOCAS_REGION = 13122;

    private static final int SOTETSEG_REGION = 13123;

    private static final int SOTETSEG_MAZE_REGION = 13379;

    private static final int NYLOCAS_WAVES_TOTAL = 31;

    private static final int TICK_LENGTH = 600;

    private static final int MAIDEN_ID = 25748;

    private static final int BLOAT_ID = 25749;

    private static final int NYLOCAS_ID = 25750;

    private static final int SOTETSEG_ID = 25751;

    private static final int XARPUS_ID = 25752;

    private static final int VERZIK_ID = 22473;

    private static final Pattern MAIDEN_WAVE = Pattern.compile("Wave 'The Maiden of Sugadinti idk19' \\(.*\\) complete!Duration: (\\d+):(\\d+)\\.?(\\d+)");

    private static final Pattern BLOAT_WAVE = Pattern.compile("Wave 'The Pestilent Bloat' \\(.*\\) complete!Duration: (\\d+):(\\d+)\\.?(\\d+)");

    private static final Pattern NYLOCAS_WAVE = Pattern.compile("Wave 'The Nylocas' \\(.*\\) complete!Duration: (\\d+):(\\d+)\\.?(\\d+)");

    private static final Pattern SOTETSEG_WAVE = Pattern.compile("Wave 'Sotetseg' \\(.*\\) complete!Duration: (\\d+):(\\d+)\\.?(\\d+)");

    private static final Pattern XARPUS_WAVE = Pattern.compile("Wave 'Xarpus' \\(.*\\) complete!Duration: (\\d+):(\\d+)\\.?(\\d+)");

    private static final Pattern VERZIK_WAVE = Pattern.compile("Wave 'The Final Challenge' \\(.*\\) complete!Duration: (\\d+):(\\d+)\\.?(\\d+)");

    private static final Pattern COMPLETION = Pattern.compile("Theatre of Blood total completion time:Duration: (\\d+):(\\d+)\\.?(\\d+)");

    private static final Set<Integer> NYLOCAS_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8344), Integer.valueOf(8347), Integer.valueOf(8350), Integer.valueOf(8353),
            Integer.valueOf(10776), Integer.valueOf(10779), new Integer[] {
                    Integer.valueOf(10782), Integer.valueOf(10785),
                    Integer.valueOf(10793), Integer.valueOf(10796), Integer.valueOf(10799), Integer.valueOf(10802),
                    Integer.valueOf(8343), Integer.valueOf(8346), Integer.valueOf(8349), Integer.valueOf(8352),
                    Integer.valueOf(10775), Integer.valueOf(10778), Integer.valueOf(10781), Integer.valueOf(10784),
                    Integer.valueOf(10792), Integer.valueOf(10795), Integer.valueOf(10798), Integer.valueOf(10801),
                    Integer.valueOf(8342), Integer.valueOf(8345),
                    Integer.valueOf(8348), Integer.valueOf(8351),
                    Integer.valueOf(10774), Integer.valueOf(10777), Integer.valueOf(10780), Integer.valueOf(10783),
                    Integer.valueOf(10791), Integer.valueOf(10794), Integer.valueOf(10797), Integer.valueOf(10800) });

    private static final Set<Point> NYLOCAS_VALID_SPAWNS = (Set<Point>)ImmutableSet.of(new Point(17, 24), new Point(17, 25), new Point(18, 24), new Point(18, 25), new Point(31, 9), new Point(31, 10), new Point[] { new Point(32, 9), new Point(32, 10), new Point(46, 24), new Point(46, 25), new Point(47, 24), new Point(47, 25) });

    private static final Set<String> BOSS_NAMES = (Set<String>)ImmutableSet.of("The Maiden of Sugadinti", "Pestilent Bloat", "Nylocas Vasilias", "Sotetseg", "Xarpus", "Verzik Vitur", new String[0]);

    private static final String MAIDEN = "Maiden";

    private static final String BLOAT = "Bloat";

    private static final String NYLO = "Nylo";

    private static final String SOTETSEG = "Sote";

    private static final String XARPUS = "Xarpus";

    private static final String VERZIK = "Verzik";

    @Inject
    private Client client;

    @Inject
    private SpoonTobStatsConfig config;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ConfigManager configManager;

    private SpoonTobStatsInfobox maidenInfoBox;

    private SpoonTobStatsInfobox bloatInfoBox;

    private SpoonTobStatsInfobox nyloInfoBox;

    private SpoonTobStatsInfobox soteInfoBox;

    private SpoonTobStatsInfobox xarpusInfoBox;

    private SpoonTobStatsInfobox verzikInfoBox;

    @Inject
    protected OverlayManager overlayManager;

    @Inject
    private SpoonTobStatsOverlay overlay;

    private NavigationButton navButton;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private MaidenFlash flashOverlay;

    @Inject
    private ClientThread clientThread;

    private int prevRegion;

    private boolean tobInside;

    private boolean instanced;

    private boolean preciseTimers;

    public boolean isTobInside() {
        return this.tobInside;
    }

    private int maidenStartTick = -1;

    private boolean maiden70;

    private int maiden70time;

    private boolean maiden50;

    private int maiden50time;

    private boolean maiden30;

    private int maiden30time;

    private int maidenProcTime;

    private boolean flash = false;

    boolean isFlash() {
        return this.flash;
    }

    public void setFlash(boolean flash) {
        this.flash = flash;
    }

    private int bloatStartTick = -1;

    private int nyloStartTick = -1;

    private int currentNylos;

    private boolean nyloWavesFinished;

    private boolean nyloCleanupFinished;

    private boolean waveThisTick = false;

    private int waveTime;

    private int cleanupTime;

    private int bossSpawnTime;

    private int nyloWave = 0;

    private int soteStartTick = -1;

    private boolean sote66;

    private int sote66time;

    private boolean sote33;

    private int sote33time;

    private int xarpusStartTick = -1;

    private int xarpusAcidTime;

    private int xarpusRecoveryTime;

    private int xarpusPreScreech;

    private int xarpusPreScreechTotal;

    private int verzikStartTick = -1;

    private int verzikP1time;

    private int verzikP2time;

    private double verzikP1personal;

    private double verzikP1total;

    private double verzikP2personal;

    private double verzikP2total;

    private double verzikP2healed;

    private NPC verziknpc;

    boolean verzikRedTimerFlag = false;

    private int verzikRedCrabTime;

    public static final File TIMES_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + RuneLite.RUNELITE_DIR.getPath() + "times");

    public ArrayList<String> timeFileStr = new ArrayList<>();

    public String mode = "";

    private final Map<String, Integer> room = new HashMap<>();

    public Map<String, Integer> getRoom() {
        return this.room;
    }

    private final Map<String, Integer> time = new HashMap<>();

    public Map<String, Integer> getTime() {
        return this.time;
    }

    private final LinkedList<String> phase = new LinkedList<>();

    public LinkedList<String> getPhase() {
        return this.phase;
    }

    private final Map<String, Integer> phaseTime = new HashMap<>();

    public Map<String, Integer> getPhaseTime() {
        return this.phaseTime;
    }

    private final Map<String, Integer> phaseSplit = new HashMap<>();

    public Map<String, Integer> getPhaseSplit() {
        return this.phaseSplit;
    }

    private final Map<String, Integer> personalDamage = new HashMap<>();

    private final Map<String, Integer> totalDamage = new HashMap<>();

    private final Map<String, Integer> totalHealing = new HashMap<>();

    @Provides
    SpoonTobStatsConfig provideConfig(ConfigManager configManager) {
        return (SpoonTobStatsConfig)configManager.getConfig(SpoonTobStatsConfig.class);
    }

    protected void shutDown() throws Exception {
        resetAll();
        resetAllInfoBoxes();
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlayManager.remove(this.flashOverlay);
    }

    protected void startUp() {
        this.overlayManager.add((Overlay)this.overlay);
        this.overlayManager.add(this.flashOverlay);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (this.client.getLocalPlayer() == null)
            return;
        int tobVar = this.client.getVarbitValue(6440);
        this.tobInside = (tobVar == 2 || tobVar == 3);
        if (!this.tobInside)
            resetAll();
        int region = WorldPoint.fromLocalInstance(this.client, this.client.getLocalPlayer().getLocalLocation()).getRegionID();
        int status = this.client.getVarbitValue(6447);
        if (status == 1 && region != this.prevRegion && region != 13379) {
            this.prevRegion = region;
            if (region == 13125 &&
                    this.bloatStartTick == -1) {
                this.bloatStartTick = this.client.getTickCount();
                resetTimer();
                this.room.put("Bloat", Integer.valueOf(this.bloatStartTick));
            }
        }
        int bosshp = this.client.getVarbitValue(6448);
        if (region == 14642) {
            resetMaiden();
            resetBloat();
            resetNylo();
            resetSote();
            resetXarpus();
            resetVerzik();
            resetTimer();
        }
    }

    @Subscribe
    protected void onAnimationChanged(AnimationChanged animationChanged) {
        if (this.tobInside) {
            int id = animationChanged.getActor().getAnimation();
            if (id == 1816 && this.soteStartTick != -1) {
                int ticks = this.client.getTickCount() - this.soteStartTick;
                String P1 = "P1";
                String P2 = "P2";
                if (this.phaseTime.get(P1) == null) {
                    phase(P1, ticks, false, "Sote", null);
                    this.sote66 = true;
                    this.sote66time = this.client.getTickCount() - this.soteStartTick;
                } else if (this.phaseTime.get(P2) == null && ticks > ((Integer)this.phaseTime.get(P1)).intValue() + 10) {
                    phase(P2, ticks, true, "Sote", null);
                    this.sote33 = true;
                    this.sote33time = this.client.getTickCount() - this.soteStartTick;
                }
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) throws Exception {
        if (this.tobInside && event.getType() == ChatMessageType.GAMEMESSAGE) {
            String strippedMessage = Text.removeTags(event.getMessage());
            List<String> messages = new ArrayList<>(Collections.emptyList());
            if (MAIDEN_WAVE.matcher(strippedMessage).find()) {
                double personal = ((Integer)this.personalDamage.getOrDefault("The Maiden of Sugadinti", Integer.valueOf(0))).intValue();
                double total = ((Integer)this.totalDamage.getOrDefault("The Maiden of Sugadinti", Integer.valueOf(0))).intValue();
                int healed = ((Integer)this.totalHealing.getOrDefault("The Maiden of Sugadinti", Integer.valueOf(0))).intValue();
                String healing = "Total Healing - " + DMG_FORMAT.format(healed);
                double percent = personal / total * 100.0D;
                String roomTime = "";
                String splits = "";
                String damage = "";
                messages.clear();
                if (this.maidenStartTick > 0) {
                    int roomTicks = this.client.getTickCount() - this.maidenStartTick;
                    phase("Maiden", roomTicks, true, "Maiden", event);
                    roomTime = formatTime(roomTicks);
                    splits = "70% - " + formatTime(this.maiden70time) + "</br>50% - " + formatTime(this.maiden50time) + " (" + formatTime(this.maiden50time - this.maiden70time) + ")</br>30% - " + formatTime(this.maiden30time) + " (" + formatTime(this.maiden30time - this.maiden50time) + ")</br>Room Complete - " + roomTime + " (" + formatTime(roomTicks - this.maiden30time) + ")";
                    if (this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ROOM_END)
                        if (this.config.simpleMessage()) {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "70%  - <col=ff0000>" + formatTime(this.maiden70time) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "50%  - <col=ff0000>" + formatTime(this.maiden50time) + "</col> (<col=ff0000>" +
                                    formatTime(this.maiden50time - this.maiden70time) + "</col>)", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "30% - <col=ff0000>" + formatTime(this.maiden30time) + "</col> (<col=ff0000>" +
                                    formatTime(this.maiden30time - this.maiden50time) + "</col>)", null);
                            this.timeFileStr.add("70% - " + formatTime(this.maiden70time));
                            this.timeFileStr.add("50% - " + formatTime(this.maiden50time));
                            this.timeFileStr.add("30% - " + formatTime(this.maiden30time));
                        } else {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Maiden - 70%' completed! Duration: <col=ff0000>" +
                                    formatTime(this.maiden70time) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Maiden - 50%' completed! Duration: <col=ff0000>" +
                                    formatTime(this.maiden50time) + "</col> (<col=ff0000>" + formatTime(this.maiden50time - this.maiden70time) + "</col>)", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Maiden - 30%' completed! Duration: <col=ff0000>" +
                                    formatTime(this.maiden30time) + "</col> (<col=ff0000>" + formatTime(this.maiden30time - this.maiden50time) + "</col>)", null);
                            this.timeFileStr.add("Wave 'Maiden - 70%' completed! Duration: " + formatTime(this.maiden70time));
                            this.timeFileStr.add("Wave 'Maiden - 50%' completed! Duration: " + formatTime(this.maiden50time));
                            this.timeFileStr.add("Wave 'Maiden - 30%' completed! Duration: " + formatTime(this.maiden30time));
                        }
                }
                if (personal > 0.0D) {
                    damage = "Personal Boss Damage - " + DMG_FORMAT.format(personal);
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Personal Boss Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(personal) + " (" + DMG_FORMAT.format(personal) + "%)")
                                .build());
                }
                if (this.config.healMsg())
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("Total Healing - ")
                            .append(Color.RED, DMG_FORMAT.format(healed))
                            .build());
                this.maidenInfoBox = createInfoBox(25748, "Maiden", roomTime, DECIMAL_FORMAT.format(percent), damage, splits, healing);
                this.infoBoxManager.addInfoBox(this.maidenInfoBox);
                resetMaiden();
            } else if (BLOAT_WAVE.matcher(strippedMessage).find()) {
                double personal = ((Integer)this.personalDamage.getOrDefault("Pestilent Bloat", Integer.valueOf(0))).intValue();
                double total = ((Integer)this.totalDamage.getOrDefault("Pestilent Bloat", Integer.valueOf(0))).intValue();
                double percent = personal / total * 100.0D;
                Matcher m = BLOAT_WAVE.matcher(strippedMessage);
                String roomTime = "";
                if (m.find())
                    if (this.preciseTimers) {
                        roomTime = m.group(1) + ":" + m.group(1) + "." + m.group(2);
                    } else {
                        roomTime = m.group(1) + ":" + m.group(1);
                    }
                String damage = "";
                messages.clear();
                if (this.bloatStartTick > 0) {
                    int roomTicks = this.client.getTickCount() - this.bloatStartTick;
                    phase("Bloat", roomTicks, false, "Bloat", event);
                }
                if (personal > 0.0D) {
                    damage = "Personal Boss Damage - " + DMG_FORMAT.format(personal);
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Personal Boss Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(personal) + " (" + DMG_FORMAT.format(personal) + "%)")
                                .build());
                }
                this.bloatInfoBox = createInfoBox(25749, "Bloat", roomTime, DECIMAL_FORMAT.format(percent), damage, "Room Complete - " + roomTime, "");
                this.infoBoxManager.addInfoBox(this.bloatInfoBox);
                resetBloat();
            } else if (NYLOCAS_WAVE.matcher(strippedMessage).find()) {
                double personal = ((Integer)this.personalDamage.getOrDefault("Nylocas Vasilias", Integer.valueOf(0))).intValue();
                double total = ((Integer)this.totalDamage.getOrDefault("Nylocas Vasilias", Integer.valueOf(0))).intValue();
                int healed = ((Integer)this.totalHealing.getOrDefault("Nylocas Vasilias", Integer.valueOf(0))).intValue();
                String healing = "Total Healing - " + DMG_FORMAT.format(healed);
                double percent = personal / total * 100.0D;
                String roomTime = "";
                String splits = "";
                String damage = "";
                messages.clear();
                if (this.nyloStartTick > 0) {
                    int roomTicks = this.client.getTickCount() - this.nyloStartTick;
                    phase("Nylo", roomTicks, true, "Nylo", event);
                    roomTime = formatTime(roomTicks);
                    splits = "Waves - " + formatTime(this.waveTime) + "</br>Cleanup - " + formatTime(this.cleanupTime) + " (" + formatTime(this.cleanupTime - this.waveTime) + ")</br>Boss Spawn - " + formatTime(this.bossSpawnTime) + " (" + formatTime(this.bossSpawnTime - this.cleanupTime) + ")</br>Room Complete - " + roomTime + " (" + formatTime(roomTicks - this.bossSpawnTime) + ")";
                    if (this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ROOM_END)
                        if (this.config.simpleMessage()) {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Waves - <col=ff0000>" + formatTime(this.waveTime) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Cleanup - <col=ff0000>" + formatTime(this.cleanupTime) + "</col> (<col=ff0000>" +
                                    formatTime(this.cleanupTime - this.waveTime) + "</col>)", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Boss Spawn - <col=ff0000>" + formatTime(this.bossSpawnTime) + "</col> (<col=ff0000>" +
                                    formatTime(this.bossSpawnTime - this.cleanupTime) + "</col>)", null);
                            this.timeFileStr.add("Waves - " + formatTime(this.waveTime));
                            this.timeFileStr.add("Cleanup - " + formatTime(this.cleanupTime) + " (" + formatTime(this.cleanupTime - this.waveTime) + ")");
                            this.timeFileStr.add("Boss Spawn - " + formatTime(this.bossSpawnTime) + " (" + formatTime(this.bossSpawnTime - this.cleanupTime) + ")");
                        } else {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Nylo - Waves' completed! Duration: <col=ff0000>" +
                                    formatTime(this.waveTime) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Nylo - Cleanup' completed! Duration: <col=ff0000>" +
                                    formatTime(this.cleanupTime) + "</col> (<col=ff0000>" + formatTime(this.cleanupTime - this.waveTime) + "</col>)", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Nylo - Boss Spawn' completed! Duration: <col=ff0000>" +
                                    formatTime(this.bossSpawnTime) + "</col> (<col=ff0000>" + formatTime(this.bossSpawnTime - this.cleanupTime) + "</col>)", null);
                            this.timeFileStr.add("Wave 'Nylo - Waves' completed! Duration: " + formatTime(this.waveTime));
                            this.timeFileStr.add("Wave 'Nylo - Cleanup' completed! Duration: " + formatTime(this.cleanupTime) + " (" + formatTime(this.cleanupTime - this.waveTime) + ")");
                            this.timeFileStr.add("Wave 'Nylo - Boss Spawn' completed! Duration: " + formatTime(this.bossSpawnTime) + " (" + formatTime(this.bossSpawnTime - this.cleanupTime) + ")");
                        }
                }
                if (personal > 0.0D) {
                    damage = "Personal Boss Damage - " + DMG_FORMAT.format(personal);
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Personal Boss Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(personal) + " (" + DMG_FORMAT.format(personal) + "%)")
                                .build());
                }
                if (this.config.healMsg())
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("Total Healing - ")
                            .append(Color.RED, DMG_FORMAT.format(healed))
                            .build());
                this.nyloInfoBox = createInfoBox(25750, "Nylocas", roomTime, DECIMAL_FORMAT.format(percent), damage, splits, healing);
                this.infoBoxManager.addInfoBox(this.nyloInfoBox);
                resetNylo();
            } else if (SOTETSEG_WAVE.matcher(strippedMessage).find()) {
                double personal = ((Integer)this.personalDamage.getOrDefault("Sotetseg", Integer.valueOf(0))).intValue();
                double total = ((Integer)this.totalDamage.getOrDefault("Sotetseg", Integer.valueOf(0))).intValue();
                double percent = personal / total * 100.0D;
                String roomTime = "";
                String splits = "";
                String damage = "";
                messages.clear();
                if (this.soteStartTick > 0) {
                    int roomTicks = this.client.getTickCount() - this.soteStartTick;
                    phase("Sote", roomTicks, true, "Sote", event);
                    roomTime = formatTime(roomTicks);
                    splits = "66% - " + formatTime(this.sote66time) + "</br>33% - " + formatTime(this.sote33time) + " (" + formatTime(this.sote33time - this.sote66time) + ")</br>Room Complete - " + roomTime + " (" + formatTime(roomTicks - this.sote33time) + ")";
                    if (this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ROOM_END)
                        if (this.config.simpleMessage()) {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "P1 - <col=ff0000>" + formatTime(this.sote66time) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "P2 - <col=ff0000>" + formatTime(this.sote33time - this.sote66time) + "</col> (<col=ff0000>" +
                                    formatTime(this.sote33time) + "</col>)", null);
                            this.timeFileStr.add("P1 - " + formatTime(this.sote66time));
                            this.timeFileStr.add("P2 - " + formatTime(this.sote33time - this.sote66time) + " (" + formatTime(this.sote33time) + ")");
                        } else {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Sote - P1' completed! Duration: <col=ff0000>" +
                                    formatTime(this.sote66time) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Sote - P2' completed! Duration: <col=ff0000>" +
                                    formatTime(this.sote33time) + "</col> (<col=ff0000>" + formatTime(this.sote33time - this.sote66time) + "</col>)", null);
                            this.timeFileStr.add("Wave 'Sote - P1' completed! Duration: " + formatTime(this.sote66time));
                            this.timeFileStr.add("Wave 'Sote - P2' completed! Duration: " + formatTime(this.sote33time) + " (" + formatTime(this.sote33time - this.sote66time) + ")");
                        }
                }
                if (personal > 0.0D) {
                    damage = "Personal Boss Damage - " + DMG_FORMAT.format(personal);
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Personal Boss Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(personal) + " (" + DMG_FORMAT.format(personal) + "%)")
                                .build());
                }
                this.soteInfoBox = createInfoBox(25751, "Sotetseg", roomTime, DECIMAL_FORMAT.format(percent), damage, splits, "");
                this.infoBoxManager.addInfoBox(this.soteInfoBox);
                resetSote();
            } else if (XARPUS_WAVE.matcher(strippedMessage).find()) {
                double personal = ((Integer)this.personalDamage.getOrDefault("Xarpus", Integer.valueOf(0))).intValue();
                double total = ((Integer)this.totalDamage.getOrDefault("Xarpus", Integer.valueOf(0))).intValue();
                int healed = ((Integer)this.totalHealing.getOrDefault("Xarpus", Integer.valueOf(0))).intValue();
                String healing = "Total Healing - " + DMG_FORMAT.format(healed);
                double xarpusPostScreech = personal - this.xarpusPreScreech;
                double personalPercent = personal / total * 100.0D;
                double preScreechPercent = this.xarpusPreScreech / this.xarpusPreScreechTotal * 100.0D;
                double postScreechPercent = xarpusPostScreech / (total - this.xarpusPreScreechTotal) * 100.0D;
                String roomTime = "";
                String splits = "";
                String damage = "";
                messages.clear();
                if (this.xarpusStartTick > 0) {
                    int roomTicks = this.client.getTickCount() - this.xarpusStartTick;
                    phase("Xarpus", roomTicks, true, "Xarpus", event);
                    roomTime = formatTime(roomTicks);
                    splits = "Recovery Phase - " + formatTime(this.xarpusRecoveryTime) + "</br>Screech Time - " + formatTime(this.xarpusAcidTime) + " (" + formatTime(this.xarpusAcidTime - this.xarpusRecoveryTime) + ")</br>Room Complete - " + roomTime + " (" + formatTime(roomTicks - this.xarpusAcidTime) + ")";
                    if (this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ROOM_END)
                        if (this.config.simpleMessage()) {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Recovery - <col=ff0000>" + formatTime(this.xarpusRecoveryTime) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Acid - <col=ff0000>" + formatTime(this.xarpusAcidTime) + "</col> (<col=ff0000>" +
                                    formatTime(this.xarpusAcidTime - this.xarpusRecoveryTime) + "</col>)", null);
                            this.timeFileStr.add("Recovery - " + formatTime(this.xarpusRecoveryTime));
                            this.timeFileStr.add("Acid - " + formatTime(this.xarpusAcidTime) + " (" + formatTime(this.xarpusAcidTime - this.xarpusRecoveryTime) + ")");
                        } else {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Xarpus - Recovery' completed! Duration: <col=ff0000>" +
                                    formatTime(this.xarpusRecoveryTime) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Xarpus - Acid' completed! Duration: <col=ff0000>" +
                                    formatTime(this.xarpusAcidTime) + "</col> (<col=ff0000>" + formatTime(this.xarpusAcidTime - this.xarpusRecoveryTime) + "</col>)", null);
                            this.timeFileStr.add("Wave 'Xarpus - Recovery' completed! Duration: " + formatTime(this.xarpusRecoveryTime));
                            this.timeFileStr.add("Wave 'Xarpus - Acid' completed! Duration: " + formatTime(this.xarpusAcidTime) + " (" + formatTime(this.xarpusAcidTime - this.xarpusRecoveryTime) + ")");
                        }
                }
                if (this.xarpusPreScreech > 0) {
                    damage = damage + "Pre Screech Damage - " + damage + " (" + DMG_FORMAT.format(this.xarpusPreScreech) + "%)</br>";
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Pre Screech Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(this.xarpusPreScreech) + " (" + DMG_FORMAT.format(this.xarpusPreScreech) + "%)")
                                .build());
                }
                if (xarpusPostScreech > 0.0D) {
                    damage = damage + "Post Screech Damage - " + damage + " (" + DMG_FORMAT.format(xarpusPostScreech) + "%)</br>";
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Post Screech Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(xarpusPostScreech) + " (" + DMG_FORMAT.format(xarpusPostScreech) + "%)")
                                .build());
                }
                if (personal > 0.0D) {
                    damage = damage + "Personal Boss Damage - " + damage;
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Personal Boss Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(personal) + " (" + DMG_FORMAT.format(personal) + "%)")
                                .build());
                }
                if (this.config.healMsg())
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("Total Healed - ")
                            .append(Color.RED, DMG_FORMAT.format(healed))
                            .build());
                this.xarpusInfoBox = createInfoBox(25752, "Xarpus", roomTime, DECIMAL_FORMAT.format(personalPercent), damage, splits, healing);
                this.infoBoxManager.addInfoBox(this.xarpusInfoBox);
                resetXarpus();
            } else if (VERZIK_WAVE.matcher(strippedMessage).find()) {
                double personal = ((Integer)this.personalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                double total = ((Integer)this.totalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                double p3personal = ((Integer)this.personalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP1personal + this.verzikP2personal;
                double p3total = ((Integer)this.totalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP1total + this.verzikP2total;
                double p3healed = ((Integer)this.totalHealing.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP2healed;
                double healed = ((Integer)this.totalHealing.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                double p3percent = p3personal / p3total * 100.0D;
                double p1percent = this.verzikP1personal / this.verzikP1total * 100.0D;
                double p2percent = this.verzikP2personal / this.verzikP2total * 100.0D;
                double percent = personal / total * 100.0D;
                String roomTime = "";
                String splits = "";
                String damage = "";
                String healing = "P2 Healed - " + DMG_FORMAT.format(this.verzikP2healed) + "</br>P3 Healed - " + DMG_FORMAT.format(p3healed) + "</br>Total Healed - " + DMG_FORMAT.format(healed);
                messages.clear();
                if (this.verzikStartTick > 0) {
                    int roomTicks = this.client.getTickCount() - this.verzikStartTick;
                    phase("Verzik", roomTicks, true, "Verzik", event);
                    roomTime = formatTime(roomTicks);
                    splits = "P1 - " + formatTime(this.verzikP1time) + "</br>P2 - " + formatTime(this.verzikP2time) + " (" + formatTime(this.verzikP2time - this.verzikP1time) + ")</br>P3 - " + roomTime + " (" + formatTime(roomTicks - this.verzikP2time) + ")";
                    if (this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ROOM_END)
                        if (this.config.simpleMessage()) {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "P1 - <col=ff0000>" + formatTime(this.verzikP1time) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Reds - <col=ff0000>" + formatTime(this.verzikRedCrabTime) + "</col> (<col=ff0000>" +
                                    formatTime(this.verzikRedCrabTime - this.verzikP1time) + "</col>)", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "P2 - <col=ff0000>" + formatTime(this.verzikP2time - this.verzikP1time) + "</col> (<col=ff0000>" +
                                    formatTime(this.verzikP2time) + "</col>)", null);
                            this.timeFileStr.add("P1 - " + formatTime(this.verzikP1time));
                            this.timeFileStr.add("Reds - " + formatTime(this.verzikRedCrabTime) + " (" + formatTime(this.verzikRedCrabTime - this.verzikP1time) + ")");
                            this.timeFileStr.add("P2 - " + formatTime(this.verzikP2time - this.verzikP1time) + " (" + formatTime(this.verzikP2time) + ")");
                        } else {
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Verzik - P1' completed! Duration: <col=ff0000>" +
                                    formatTime(this.verzikP1time) + "</col>", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Verzik - Reds' completed! Duration: <col=ff0000>" +
                                    formatTime(this.verzikRedCrabTime) + "</col> (<col=ff0000>" + formatTime(this.verzikRedCrabTime - this.verzikP1time) + "</col>)", null);
                            this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Wave 'Verzik - P2' completed! Duration: <col=ff0000>" +
                                    formatTime(this.verzikP2time) + "</col> (<col=ff0000>" + formatTime(this.verzikP2time - this.verzikP1time) + "</col>)", null);
                            this.timeFileStr.add("Wave 'Verzik - P1' completed! Duration: " + formatTime(this.verzikP1time));
                            this.timeFileStr.add("Wave 'Verzik - Reds' completed! Duration: " + formatTime(this.verzikRedCrabTime) + " (" + formatTime(this.verzikRedCrabTime - this.verzikP1time) + ")");
                            this.timeFileStr.add("Wave 'Verzik - P2' completed! Duration: " + formatTime(this.verzikP2time) + " (" + formatTime(this.verzikP2time - this.verzikP1time) + ")");
                        }
                }
                if (this.verzikP1personal > 0.0D)
                    damage = damage + "P1 Personal Damage - " + damage + " (" + DMG_FORMAT.format(this.verzikP1personal) + "%)</br>";
                if (this.verzikP2personal > 0.0D)
                    damage = damage + "P2 Personal Damage - " + damage + " (" + DMG_FORMAT.format(this.verzikP2personal) + "%)</br>";
                if (p3personal > 0.0D)
                    damage = damage + "P3 Personal Damage - " + damage + " (" + DMG_FORMAT.format(p3personal) + "%)</br>";
                if (personal > 0.0D)
                    damage = damage + "Total Personal Damage - " + damage;
                this.verzikInfoBox = createInfoBox(22473, "Verzik", roomTime, DECIMAL_FORMAT.format(percent), damage, splits, healing);
                this.infoBoxManager.addInfoBox(this.verzikInfoBox);
                resetVerzik();
            } else if (strippedMessage.contains("Your completed Theatre of Blood: ") && strippedMessage.contains(" count is:")) {
                this.timeFileStr.add(0, strippedMessage);
                if (this.config.timeExporter() && !this.mode.equals(""))
                    exportTimes();
            } else if (strippedMessage.contains("You enter the Theatre of Blood (") && strippedMessage.contains("Mode)...")) {
                if (strippedMessage.contains("(Entry Mode)")) {
                    this.mode = "SM";
                } else if (strippedMessage.contains("(Normal Mode)")) {
                    this.mode = "REG";
                } else if (strippedMessage.contains("(Hard Mode)")) {
                    this.mode = "HM";
                }
            }
            if (this.config.oldRoomMsg() && event.getMessage().contains("Wave '") && event.getMessage().contains(" Mode) complete!<br>Duration: <col=ff0000>"))
                if (Text.removeTags(event.getMessage()).contains("(Hard Mode)")) {
                    event.getMessageNode().setValue(event.getMessageNode().getValue().replace("(Hard Mode) complete!<br>", "complete! "));
                    this.timeFileStr.add(Text.removeTags(event.getMessageNode().getValue().replace("(Hard Mode) complete!<br>", "complete! ")));
                } else if (Text.removeTags(event.getMessage()).contains("(Entry Mode)")) {
                    event.getMessageNode().setValue(event.getMessageNode().getValue().replace("(Entry Mode) complete!<br>", "complete! "));
                    this.timeFileStr.add(Text.removeTags(event.getMessageNode().getValue().replace("(Entry Mode) complete!<br>", "complete! ")));
                } else if (Text.removeTags(event.getMessage()).contains("(Normal Mode)")) {
                    event.getMessageNode().setValue(event.getMessageNode().getValue().replace("(Normal Mode) complete!<br>", "complete! "));
                    this.timeFileStr.add(Text.removeTags(event.getMessageNode().getValue().replace("(Normal Mode) complete!<br>", "complete! ")));
                }
            if (!messages.isEmpty()) {
                for (String m : messages)
                    this.chatMessageManager.queue(QueuedMessage.builder()
                            .type(ChatMessageType.GAMEMESSAGE)
                            .runeLiteFormattedMessage(m)
                            .build());
                messages.clear();
            }
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        double personal, total, p3personal, p3total, p3healed, healed, p3percent, percent;
        if (!this.tobInside)
            return;
        List<String> messages = new ArrayList<>(Collections.emptyList());
        NPC npc = event.getNpc();
        int npcId = npc.getId();
        switch (npcId) {
            case 8361:
            case 10815:
            case 10823:
                if (this.maidenStartTick != -1 && !this.maiden70) {
                    this.maiden70 = true;
                    this.maiden70time = this.client.getTickCount() - this.maidenStartTick;
                    this.maidenProcTime = this.client.getTickCount();
                    phase("70%", this.maiden70time, false, "Maiden", null);
                }
                break;
            case 8362:
            case 10816:
            case 10824:
                if (this.maidenStartTick != -1 && !this.maiden50) {
                    this.maiden50 = true;
                    this.maiden50time = this.client.getTickCount() - this.maidenStartTick;
                    this.maidenProcTime = this.client.getTickCount();
                    phase("50%", this.maiden50time, true, "Maiden", null);
                    if ((this.maiden50time - this.maiden70time) * 600 < 10600)
                        this.flash = true;
                }
                break;
            case 8363:
            case 10817:
            case 10825:
                if (this.maidenStartTick != -1 && !this.maiden30) {
                    this.maiden30 = true;
                    this.maiden30time = this.client.getTickCount() - this.maidenStartTick;
                    this.maidenProcTime = this.client.getTickCount();
                    phase("30%", this.maiden30time, true, "Maiden", null);
                    if ((this.maiden30time - this.maiden50time) * 600 < 10600)
                        this.flash = true;
                }
                break;
            case 8388:
            case 10865:
            case 10868:
                if (this.soteStartTick == -1) {
                    this.soteStartTick = this.client.getTickCount();
                    resetTimer();
                    this.room.put("Sote", Integer.valueOf(this.soteStartTick));
                }
                break;
            case 8339:
            case 10767:
            case 10771:
                this.xarpusStartTick = this.client.getTickCount();
                resetTimer();
                this.room.put("Xarpus", Integer.valueOf(this.xarpusStartTick));
                break;
            case 8340:
            case 10768:
            case 10772:
                this.xarpusRecoveryTime = this.client.getTickCount() - this.xarpusStartTick;
                phase("Recovery", this.xarpusRecoveryTime, false, "Xarpus", null);
                break;
            case 8370:
            case 10831:
            case 10848:
                this.verzikStartTick = this.client.getTickCount();
                resetTimer();
                this.room.put("Verzik", Integer.valueOf(this.verzikStartTick));
                break;
            case 8375:
            case 10836:
            case 10853:
                personal = ((Integer)this.personalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                total = ((Integer)this.totalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                p3personal = ((Integer)this.personalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP1personal + this.verzikP2personal;
                p3total = ((Integer)this.totalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP1total + this.verzikP2total;
                p3healed = ((Integer)this.totalHealing.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP2healed;
                healed = ((Integer)this.totalHealing.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                p3percent = p3personal / p3total * 100.0D;
                percent = personal / total * 100.0D;
                if (p3personal > 0.0D &&
                        this.config.dmgMsg())
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("P3 Personal Damage - ")
                            .append(Color.RED, DMG_FORMAT.format(p3personal) + " (" + DMG_FORMAT.format(p3personal) + "%)")
                            .build());
                if (personal > 0.0D)
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("Total Personal Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(personal) + " (" + DMG_FORMAT.format(personal) + "%)")
                                .build());
                if (this.config.healMsg()) {
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("P3 Healed - ")
                            .append(Color.RED, DMG_FORMAT.format(p3healed))
                            .build());
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("Total Healed - ")
                            .append(Color.RED, DMG_FORMAT.format(healed))
                            .build());
                }
                break;
        }
        if (!messages.isEmpty()) {
            for (String m : messages)
                this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .runeLiteFormattedMessage(m)
                        .build());
            messages.clear();
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        double p1percent, p2percent;
        if (!this.tobInside)
            return;
        List<String> messages = new ArrayList<>(Collections.emptyList());
        NPC npc = event.getNpc();
        int npcId = npc.getId();
        if (npc.getName() != null && npc.getName().equals("Verzik Vitur"))
            this.verziknpc = npc;
        switch (npcId) {
            case 8360:
            case 10814:
            case 10822:
                this.maidenStartTick = this.client.getTickCount();
                resetTimer();
                this.room.put("Maiden", Integer.valueOf(this.maidenStartTick));
                break;
            case 8358:
            case 10790:
            case 10811:
                this.nyloStartTick = this.client.getTickCount();
                resetTimer();
                this.room.put("Nylo", Integer.valueOf(this.nyloStartTick));
                break;
            case 8354:
            case 10786:
            case 10807:
                this.bossSpawnTime = this.client.getTickCount() - this.nyloStartTick;
                phase("Boss Spawn", this.bossSpawnTime, true, "Nylo", null);
                break;
            case 8371:
            case 10832:
            case 10849:
                this.verzikP1time = this.client.getTickCount() - this.verzikStartTick;
                this.verzikP1personal = ((Integer)this.personalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                this.verzikP1total = ((Integer)this.totalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                phase("P1", this.verzikP1time, false, "Verzik", null);
                p1percent = this.verzikP1personal / this.verzikP1total * 100.0D;
                messages.clear();
                if (this.verzikP1personal > 0.0D &&
                        this.config.dmgMsg())
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("P1 Personal Damage - ")
                            .append(Color.RED, DMG_FORMAT.format(this.verzikP1personal) + " (" + DMG_FORMAT.format(this.verzikP1personal) + "%)")
                            .build());
                break;
            case 8373:
            case 10834:
            case 10851:
                this.verzikP2time = this.client.getTickCount() - this.verzikStartTick;
                this.verzikP2personal = ((Integer)this.personalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP1personal;
                this.verzikP2total = ((Integer)this.totalDamage.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue() - this.verzikP1total;
                this.verzikP2healed = ((Integer)this.totalHealing.getOrDefault("Verzik Vitur", Integer.valueOf(0))).intValue();
                this.phase.remove("Reds");
                phase("P2", this.verzikP2time, true, "Verzik", null);
                p2percent = this.verzikP2personal / this.verzikP2total * 100.0D;
                messages.clear();
                if (this.verzikP2personal > 0.0D)
                    if (this.config.dmgMsg())
                        messages.add((new ChatMessageBuilder())

                                .append(ChatColorType.NORMAL)
                                .append("P2 Personal Damage - ")
                                .append(Color.RED, DMG_FORMAT.format(this.verzikP2personal) + " (" + DMG_FORMAT.format(this.verzikP2personal) + "%)")
                                .build());
                if (this.config.healMsg())
                    messages.add((new ChatMessageBuilder())

                            .append(ChatColorType.NORMAL)
                            .append("P2 Healed - ")
                            .append(Color.RED, DMG_FORMAT.format(this.verzikP2healed))
                            .build());
                break;
        }
        if (!messages.isEmpty()) {
            for (String m : messages)
                this.chatMessageManager.queue(QueuedMessage.builder()
                        .type(ChatMessageType.GAMEMESSAGE)
                        .runeLiteFormattedMessage(m)
                        .build());
            messages.clear();
        }
        if (!NYLOCAS_IDS.contains(Integer.valueOf(npcId)) || this.prevRegion != 13122)
            return;
        this.currentNylos++;
        WorldPoint worldPoint = WorldPoint.fromLocalInstance(this.client, npc.getLocalLocation());
        Point spawnLoc = new Point(worldPoint.getRegionX(), worldPoint.getRegionY());
        if (!NYLOCAS_VALID_SPAWNS.contains(spawnLoc))
            return;
        if (!this.waveThisTick) {
            this.nyloWave++;
            this.waveThisTick = true;
        }
        if (this.nyloWave == 31 && !this.nyloWavesFinished) {
            this.waveTime = this.client.getTickCount() - this.nyloStartTick;
            this.nyloWavesFinished = true;
            phase("Waves", this.waveTime, false, "Nylo", null);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if (!this.tobInside)
            return;
        NPC npc = event.getNpc();
        int npcId = npc.getId();
        if (!NYLOCAS_IDS.contains(Integer.valueOf(npcId)) || this.prevRegion != 13122)
            return;
        this.currentNylos--;
        if (this.nyloWavesFinished && !this.nyloCleanupFinished && this.currentNylos == 0) {
            this.cleanupTime = this.client.getTickCount() - this.nyloStartTick;
            this.nyloCleanupFinished = true;
            phase("Cleanup", this.cleanupTime, true, "Nylo", null);
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!this.tobInside)
            return;
        if (this.waveThisTick)
            this.waveThisTick = false;
        if (this.verziknpc != null &&
                this.verziknpc.getAnimation() == 8117 && !this.verzikRedTimerFlag) {
            this.verzikRedCrabTime = this.client.getTickCount() - this.verzikStartTick;
            phase("Reds", this.verzikRedCrabTime, true, "Verzik", null);
            this.verzikRedTimerFlag = true;
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (this.tobInside) {
            boolean ingame_setting = (this.client.getVarbitValue(11866) == 1);
            this
                    .preciseTimers = (this.config.preciseTimers() == SpoonTobStatsConfig.PreciseTimersSetting.TICKS || (this.config.preciseTimers() == SpoonTobStatsConfig.PreciseTimersSetting.INGAME_SETTING && ingame_setting));
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() != GameState.LOADING)
            return;
        boolean prevInstance = this.instanced;
        this.instanced = this.client.isInInstancedRegion();
        if (prevInstance && !this.instanced) {
            resetAll();
            resetAllInfoBoxes();
            resetTimer();
        } else if (!prevInstance && this.instanced) {
            resetAll();
            resetAllInfoBoxes();
            resetTimer();
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (!this.tobInside)
            return;
        Actor actor = event.getActor();
        if (!(actor instanceof NPC))
            return;
        NPC npc = (NPC)actor;
        String npcName = npc.getName();
        if (npcName == null || !BOSS_NAMES.contains(npcName))
            return;
        npcName = Text.removeTags(npcName);
        Hitsplat hitsplat = event.getHitsplat();
        if (hitsplat.isMine()) {
            int myDmg = ((Integer)this.personalDamage.getOrDefault(npcName, Integer.valueOf(0))).intValue();
            int totalDmg = ((Integer)this.totalDamage.getOrDefault(npcName, Integer.valueOf(0))).intValue();
            myDmg += hitsplat.getAmount();
            totalDmg += hitsplat.getAmount();
            this.personalDamage.put(npcName, Integer.valueOf(myDmg));
            this.totalDamage.put(npcName, Integer.valueOf(totalDmg));
        } else if (hitsplat.isOthers()) {
            int totalDmg = ((Integer)this.totalDamage.getOrDefault(npcName, Integer.valueOf(0))).intValue();
            totalDmg += hitsplat.getAmount();
            this.totalDamage.put(npcName, Integer.valueOf(totalDmg));
        } else if (hitsplat.getHitsplatType() == Hitsplat.HitsplatType.HEAL) {
            int healed = ((Integer)this.totalHealing.getOrDefault(npcName, Integer.valueOf(0))).intValue();
            healed += hitsplat.getAmount();
            this.totalHealing.put(npcName, Integer.valueOf(healed));
        }
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        Actor npc = event.getActor();
        if (!(npc instanceof NPC) || !this.tobInside)
            return;
        String overheadText = event.getOverheadText();
        String npcName = npc.getName();
        if (npcName != null && npcName.equals("Xarpus") && overheadText.equals("Screeeeech!")) {
            this.xarpusAcidTime = this.client.getTickCount() - this.xarpusStartTick;
            this.xarpusPreScreech = ((Integer)this.personalDamage.getOrDefault(npcName, Integer.valueOf(0))).intValue();
            this.xarpusPreScreechTotal = ((Integer)this.totalDamage.getOrDefault(npcName, Integer.valueOf(0))).intValue();
            phase("Acid", this.xarpusAcidTime, true, "Xarpus", null);
        }
    }

    private SpoonTobStatsInfobox createInfoBox(int itemId, String room, String time, String percent, String damage, String splits, String healed) {
        AsyncBufferedImage asyncBufferedImage = this.itemManager.getImage(itemId);
        return new SpoonTobStatsInfobox((BufferedImage)asyncBufferedImage, this.config, this, room, time, percent, damage, splits, healed);
    }

    public String formatTime(int ticks) {
        int millis = ticks * 600;
        String hundredths = String.valueOf(millis % 1000).substring(0, 1);
        if (this.preciseTimers)
            return String.format("%d:%02d.%s", new Object[] { Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1L)),
                    Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1L)), hundredths });
        if (hundredths.equals("6") || hundredths.equals("8"))
            millis += 1000;
        return String.format("%d:%02d", new Object[] { Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1L)),
                Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1L)) });
    }

    public static String to_mmss_precise(int ticks) {
        int min = ticks / 100;
        int tmp = (ticks - min * 100) * 6;
        int sec = tmp / 10;
        int sec_tenth = tmp - sec * 10;
        String timeStr = "" + min + min + ((sec < 10) ? ":0" : ":") + "." + sec;
        return timeStr;
    }

    public static String to_mmss(int ticks) {
        int m = ticks / 100;
        int s = (ticks - m * 100) * 6 / 10;
        String timeStr = "" + m + m + ((s < 10) ? ":0" : ":");
        return timeStr;
    }

    private void phase(String name, int ticks, boolean splitPhase, String boss, ChatMessage event) {
        if (splitPhase && !this.phase.isEmpty())
            this.phaseSplit.put(name, Integer.valueOf(ticks - ((Integer)this.phaseTime.get(this.phase.getLast())).intValue()));
        if (!name.equals(boss)) {
            this.phaseTime.put(name, Integer.valueOf(ticks));
            this.phase.add(name);
            if (this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ACTIVE)
                if (this.config.simpleMessage()) {
                    printTime(ticks, name, ((Integer)this.phaseSplit.getOrDefault(name, Integer.valueOf(0))).intValue());
                } else {
                    printTime(ticks, boss + " - " + boss, ((Integer)this.phaseSplit.getOrDefault(name, Integer.valueOf(0))).intValue());
                }
        } else {
            this.time.put(name, Integer.valueOf(ticks));
            if ((this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ACTIVE || this.config.msgTiming() == SpoonTobStatsConfig.msgTimeMode.ROOM_END) &&
                    !this.phase.isEmpty() && event != null) {
                String string = event.getMessage();
                String[] message = string.split("(?=</col>)", 2);
                String startMessage = message[0];
                String endMessage = message[1];
                event.getMessageNode().setValue(startMessage + "</col> (<col=ff0000>" + startMessage + "</col>)" + formatTime(ticks - ((Integer)this.phaseTime.get(this.phase.getLast())).intValue()));
                this.timeFileStr.add(Text.removeTags(startMessage + "</col> (<col=ff0000>" + startMessage + "</col>)" + formatTime(ticks - ((Integer)this.phaseTime.get(this.phase.getLast())).intValue())));
            }
        }
    }

    private void printTime(int ticks, String subject, int splitTicks) {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.config.simpleMessage()) {
            stringBuilder.append(subject).append(" - ").append("<col=ff0000>").append(formatTime(ticks));
        } else {
            stringBuilder.append("Wave '").append(subject).append("' complete! Duration: <col=ff0000>").append(formatTime(ticks));
        }
        if (splitTicks > 0)
            stringBuilder.append("</col> (<col=ff0000>").append(formatTime(splitTicks)).append("</col>)");
        this.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", stringBuilder.toString(), "", false);
        this.timeFileStr.add(Text.removeTags(stringBuilder.toString()));
    }

    private void resetMaiden() {
        this.maidenStartTick = -1;
        this.maiden70 = false;
        this.maiden70time = 0;
        this.maiden50 = false;
        this.maiden50time = 0;
        this.maiden30 = false;
        this.maiden30time = 0;
        this.personalDamage.remove("The Maiden of Sugadinti");
        this.totalDamage.remove("The Maiden of Sugadinti");
        this.totalHealing.remove("The Maiden of Sugadinti");
        this.maidenProcTime = 0;
    }

    private void resetBloat() {
        this.personalDamage.remove("Pestilent Bloat");
        this.totalDamage.remove("Pestilent Bloat");
        this.bloatStartTick = -1;
    }

    private void resetNylo() {
        this.nyloStartTick = -1;
        this.currentNylos = 0;
        this.nyloWavesFinished = false;
        this.nyloCleanupFinished = false;
        this.waveTime = 0;
        this.cleanupTime = 0;
        this.bossSpawnTime = 0;
        this.waveThisTick = false;
        this.nyloWave = 0;
        this.personalDamage.remove("Nylocas Vasilias");
        this.totalDamage.remove("Nylocas Vasilias");
    }

    private void resetSote() {
        this.soteStartTick = -1;
        this.sote66 = false;
        this.sote66time = 0;
        this.sote33 = false;
        this.sote33time = 0;
        this.personalDamage.remove("Sotetseg");
        this.totalDamage.remove("Sotetseg");
    }

    private void resetXarpus() {
        this.xarpusStartTick = -1;
        this.xarpusRecoveryTime = 0;
        this.xarpusAcidTime = 0;
        this.xarpusPreScreech = 0;
        this.xarpusPreScreechTotal = 0;
        this.personalDamage.remove("Xarpus");
        this.totalDamage.remove("Xarpus");
        this.totalHealing.remove("Xarpus");
    }

    private void resetVerzik() {
        this.verzikStartTick = -1;
        this.verzikP1time = 0;
        this.verzikP2time = 0;
        this.verzikP1personal = 0.0D;
        this.verzikP1total = 0.0D;
        this.verzikP2personal = 0.0D;
        this.verzikP2total = 0.0D;
        this.verzikP2healed = 0.0D;
        this.personalDamage.clear();
        this.totalDamage.clear();
        this.totalHealing.clear();
        this.verzikRedCrabTime = 0;
        this.verzikRedTimerFlag = false;
    }

    private void resetTimer() {
        this.room.clear();
        this.time.clear();
        this.phase.clear();
        this.phaseTime.clear();
        this.phaseSplit.clear();
    }

    private void resetAll() {
        resetMaiden();
        resetBloat();
        resetNylo();
        resetSote();
        resetXarpus();
        resetVerzik();
        this.timeFileStr.clear();
        this.mode = "";
    }

    private void resetAllInfoBoxes() {
        this.infoBoxManager.removeInfoBox(this.maidenInfoBox);
        this.infoBoxManager.removeInfoBox(this.bloatInfoBox);
        this.infoBoxManager.removeInfoBox(this.nyloInfoBox);
        this.infoBoxManager.removeInfoBox(this.soteInfoBox);
        this.infoBoxManager.removeInfoBox(this.xarpusInfoBox);
        this.infoBoxManager.removeInfoBox(this.verzikInfoBox);
    }

    private void exportTimes() throws Exception {
        String fileName = "" + TIMES_DIR + "\\" + TIMES_DIR + "_" + this.client.getLocalPlayer().getName() + "_TobTimes.txt";
        FileWriter writer = new FileWriter(fileName, true);
        try {
            for (String msg : this.timeFileStr)
                writer.write(msg + "\r\n");
            writer.write("------------------------------------------------------------------------------------------------\r\n------------------------------------------------------------------------------------------------\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.close();
    }
}

