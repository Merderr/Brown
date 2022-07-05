package net.runelite.client.plugins.spoontobstats;

public enum InfoboxText {
    DAMAGE_PERCENT("Damage Percent"),
    TIME("Room Time"),
    NONE("None");

    InfoboxText(String type) {
        this.type = type;
    }

    private final String type;

    public String getType() {
        return this.type;
    }

    public String toString() {
        return this.type;
    }
}
