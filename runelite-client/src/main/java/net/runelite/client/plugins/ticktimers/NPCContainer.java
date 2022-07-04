package net.runelite.client.plugins.ticktimers;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.util.Objects;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Prayer;

class NPCContainer {
    private final NPC npc;

    private final int npcIndex;

    private final String npcName;

    private final ImmutableSet<Integer> animations;

    private final int attackSpeed;

    private final BossMonsters monsterType;

    private int npcSize;

    private int ticksUntilAttack;

    private Actor npcInteracting;

    private AttackStyle attackStyle;

    NPC getNpc() {
        return this.npc;
    }

    int getNpcIndex() {
        return this.npcIndex;
    }

    String getNpcName() {
        return this.npcName;
    }

    ImmutableSet<Integer> getAnimations() {
        return this.animations;
    }

    int getAttackSpeed() {
        return this.attackSpeed;
    }

    BossMonsters getMonsterType() {
        return this.monsterType;
    }

    int getNpcSize() {
        return this.npcSize;
    }

    void setTicksUntilAttack(int ticksUntilAttack) {
        this.ticksUntilAttack = ticksUntilAttack;
    }

    int getTicksUntilAttack() {
        return this.ticksUntilAttack;
    }

    void setNpcInteracting(Actor npcInteracting) {
        this.npcInteracting = npcInteracting;
    }

    Actor getNpcInteracting() {
        return this.npcInteracting;
    }

    void setAttackStyle(AttackStyle attackStyle) {
        this.attackStyle = attackStyle;
    }

    AttackStyle getAttackStyle() {
        return this.attackStyle;
    }

    NPCContainer(NPC npc) {
        this.npc = npc;
        this.npcName = npc.getName();
        this.npcIndex = npc.getIndex();
        this.npcInteracting = npc.getInteracting();
        this.attackStyle = AttackStyle.UNKNOWN;
        this.ticksUntilAttack = -1;
        NPCComposition composition = npc.getTransformedComposition();
        BossMonsters monster = BossMonsters.of(npc.getId());
        if (monster == null)
            throw new IllegalStateException();
        this.monsterType = monster;
        this.animations = monster.animations;
        this.attackStyle = monster.attackStyle;
        this.attackSpeed = monster.attackSpeed;
        if (composition != null)
            this.npcSize = composition.getSize();
    }

    public int hashCode() {
        return Objects.hash(new Object[] { this.npc });
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NPCContainer that = (NPCContainer)o;
        return Objects.equals(this.npc, that.npc);
    }

    enum BossMonsters {
        SERGEANT_STRONGSTACK(2216, NPCContainer.AttackStyle.MELEE, ImmutableSet.of(Integer.valueOf(6154), Integer.valueOf(6156), Integer.valueOf(7071)), 5),
        SERGEANT_STEELWILL(2217, NPCContainer.AttackStyle.MAGE, ImmutableSet.of(Integer.valueOf(6154), Integer.valueOf(6156), Integer.valueOf(7071)), 5),
        SERGEANT_GRIMSPIKE(2218, NPCContainer.AttackStyle.RANGE, ImmutableSet.of(Integer.valueOf(6154), Integer.valueOf(6156), Integer.valueOf(7073)), 5),
        GENERAL_GRAARDOR(2215, NPCContainer.AttackStyle.MELEE, ImmutableSet.of(Integer.valueOf(7018), Integer.valueOf(7020), Integer.valueOf(7021)), 6),
        TSTANON_KARLAK(3130, NPCContainer.AttackStyle.MELEE, ImmutableSet.of(Integer.valueOf(64)), 5),
        BALFRUG_KREEYATH(3132, NPCContainer.AttackStyle.MAGE, ImmutableSet.of(Integer.valueOf(64), Integer.valueOf(4630)), 5),
        ZAKLN_GRITCH(3131, NPCContainer.AttackStyle.RANGE, ImmutableSet.of(Integer.valueOf(64), Integer.valueOf(7077)), 5),
        KRIL_TSUTSAROTH(3129, NPCContainer.AttackStyle.UNKNOWN, ImmutableSet.of(Integer.valueOf(6950), Integer.valueOf(6948)), 6),
        STARLIGHT(2206, NPCContainer.AttackStyle.MELEE, ImmutableSet.of(Integer.valueOf(6376)), 5),
        GROWLER(2207, NPCContainer.AttackStyle.MAGE, ImmutableSet.of(Integer.valueOf(7037)), 5),
        BREE(2208, NPCContainer.AttackStyle.RANGE, ImmutableSet.of(Integer.valueOf(7026)), 5),
        COMMANDER_ZILYANA(2205, NPCContainer.AttackStyle.UNKNOWN, ImmutableSet.of(Integer.valueOf(6967), Integer.valueOf(6964), Integer.valueOf(6970)), 2),
        FLIGHT_KILISA(3165, NPCContainer.AttackStyle.MELEE, ImmutableSet.of(Integer.valueOf(6957)), 5),
        FLOCKLEADER_GEERIN(3164, NPCContainer.AttackStyle.RANGE, ImmutableSet.of(Integer.valueOf(6956), Integer.valueOf(6958)), 5),
        WINGMAN_SKREE(3163, NPCContainer.AttackStyle.MAGE, ImmutableSet.of(Integer.valueOf(6955)), 5),
        KREEARRA(3162, NPCContainer.AttackStyle.RANGE, ImmutableSet.of(Integer.valueOf(6978)), 3);

        BossMonsters(int npcID, NPCContainer.AttackStyle attackStyle, ImmutableSet<Integer> animations, int attackSpeed) {
            this.npcID = npcID;
            this.attackStyle = attackStyle;
            this.animations = animations;
            this.attackSpeed = attackSpeed;
        }

        private static final ImmutableMap<Integer, BossMonsters> idMap;

        private final int npcID;

        private final NPCContainer.AttackStyle attackStyle;

        private final ImmutableSet<Integer> animations;

        private final int attackSpeed;

        static {
            ImmutableMap.Builder<Integer, BossMonsters> builder = ImmutableMap.builder();
            for (BossMonsters monster : values())
                builder.put(Integer.valueOf(monster.npcID), monster);
            idMap = builder.build();
        }

        static BossMonsters of(int npcID) {
            return (BossMonsters)idMap.get(Integer.valueOf(npcID));
        }
    }

    public enum AttackStyle {
        MAGE("Mage", Color.CYAN, Prayer.PROTECT_FROM_MAGIC),
        RANGE("Range", Color.GREEN, Prayer.PROTECT_FROM_MISSILES),
        MELEE("Melee", Color.RED, Prayer.PROTECT_FROM_MELEE),
        UNKNOWN("Unknown", Color.WHITE, null);

        AttackStyle(String name, Color color, Prayer prayer) {
            this.name = name;
            this.color = color;
            this.prayer = prayer;
        }

        private String name;

        private Color color;

        private Prayer prayer;

        public String getName() {
            return this.name;
        }

        public Color getColor() {
            return this.color;
        }

        public Prayer getPrayer() {
            return this.prayer;
        }
    }
}

