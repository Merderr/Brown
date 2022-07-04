package net.runelite.client.plugins.theatre.prayer;

import java.util.Comparator;
import lombok.NonNull;
import net.runelite.api.Prayer;

public class TheatreUpcomingAttack implements Comparable<TheatreUpcomingAttack> {
    private int ticksUntil;

    private final Prayer prayer;

    private final int priority;

    public int getTicksUntil() {
        return this.ticksUntil;
    }

    public Prayer getPrayer() {
        return this.prayer;
    }

    public int getPriority() {
        return this.priority;
    }

    public TheatreUpcomingAttack(int ticksUntil, Prayer prayer, int priority) {
        this.ticksUntil = ticksUntil;
        this.prayer = prayer;
        this.priority = priority;
    }

    public TheatreUpcomingAttack(int ticksUntil, Prayer prayer) {
        this(ticksUntil, prayer, 0);
    }

    public void decrementTicks() {
        if (this.ticksUntil > 0)
            this.ticksUntil--;
    }

    public boolean shouldRemove() {
        return (this.ticksUntil == 0);
    }

    public int compareTo(@NonNull TheatreUpcomingAttack o) {
        if (o == null)
            throw new NullPointerException("o is marked non-null but is null");
        return Comparator.comparing(TheatreUpcomingAttack::getTicksUntil).thenComparing(TheatreUpcomingAttack::getPriority).compare(this, o);
    }
}
