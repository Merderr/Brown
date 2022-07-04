package net.runelite.client.plugins.theatre.Verzik;

import java.util.Objects;
import java.util.Set;
import net.runelite.api.coords.WorldPoint;

public class VerzikPoisonTile {
    private static final int VERZIK_P2_POISON_TICKS = 14;

    WorldPoint tile;

    int ticksRemaining;

    public WorldPoint getTile() {
        return this.tile;
    }

    public int getTicksRemaining() {
        return this.ticksRemaining;
    }

    public VerzikPoisonTile(WorldPoint tile) {
        this.tile = tile;
        this.ticksRemaining = 14;
    }

    public void decrement() {
        if (this.ticksRemaining > 0)
            this.ticksRemaining--;
    }

    public boolean isDead() {
        return (this.ticksRemaining == 0);
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VerzikPoisonTile that = (VerzikPoisonTile)o;
        return this.tile.equals(that.tile);
    }

    public boolean shouldHighlight() {
        return (this.ticksRemaining < 4);
    }

    public int hashCode() {
        return Objects.hash(new Object[] { this.tile });
    }

    static void updateTiles(Set<VerzikPoisonTile> tileSet) {
        tileSet.forEach(VerzikPoisonTile::decrement);
        tileSet.removeIf(VerzikPoisonTile::isDead);
    }
}
