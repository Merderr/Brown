package net.runelite.client.plugins.theatre.Verzik;

import java.util.Queue;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.prayer.TheatrePrayerOverlay;
import net.runelite.client.plugins.theatre.prayer.TheatreUpcomingAttack;

public class VerzikPrayerOverlay extends TheatrePrayerOverlay {
    private final Verzik plugin;

    @Inject
    protected VerzikPrayerOverlay(Client client, TheatreConfig config, Verzik plugin) {
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
        return getConfig().verzikPrayerHelper();
    }
}
