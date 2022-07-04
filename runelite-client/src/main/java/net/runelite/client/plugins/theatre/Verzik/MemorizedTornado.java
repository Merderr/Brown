package net.runelite.client.plugins.theatre.Verzik;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

public class MemorizedTornado {
    private NPC npc;

    private WorldPoint lastPosition;

    private WorldPoint currentPosition;

    NPC getNpc() {
        return this.npc;
    }

    WorldPoint getLastPosition() {
        return this.lastPosition;
    }

    void setLastPosition(WorldPoint lastPosition) {
        this.lastPosition = lastPosition;
    }

    WorldPoint getCurrentPosition() {
        return this.currentPosition;
    }

    void setCurrentPosition(WorldPoint currentPosition) {
        this.currentPosition = currentPosition;
    }

    MemorizedTornado(NPC npc) {
        this.npc = npc;
        this.lastPosition = null;
        this.currentPosition = null;
    }

    public int getRelativeDelta(WorldPoint pt) {
        if (this.currentPosition == null || this.lastPosition == null || this.lastPosition.distanceTo(this.currentPosition) == 0)
            return -1;
        return pt.distanceTo(this.currentPosition) - pt.distanceTo(this.lastPosition);
    }
}
