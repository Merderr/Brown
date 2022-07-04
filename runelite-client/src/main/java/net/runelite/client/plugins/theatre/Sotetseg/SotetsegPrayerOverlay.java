package net.runelite.client.plugins.theatre.Sotetseg;

import java.util.Queue;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.prayer.TheatrePrayerOverlay;
import net.runelite.client.plugins.theatre.prayer.TheatreUpcomingAttack;

public class SotetsegPrayerOverlay extends TheatrePrayerOverlay {
    private final Sotetseg plugin;

    @Inject
    protected SotetsegPrayerOverlay(Client client, TheatreConfig config, Sotetseg plugin) {
        super(client, config);
        this.plugin = plugin;
    }

    protected Queue<TheatreUpcomingAttack> getAttackQueue() {
        return this.plugin.getUpcomingAttackQueue();
    }

    protected long getLastTick() {
        return this.plugin.getLastTick();
    }

    protected boolean isEnabled() {
        return getConfig().sotetsegPrayerHelper();
    }
}
