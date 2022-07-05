package net.runelite.client.plugins.spoontobstats;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class MaidenFlash extends Overlay {
    private final Client client;

    private final SpoonTobStatsPlugin plugin;

    private final SpoonTobStatsConfig config;

    private int timeout;

    @Inject
    private MaidenFlash(Client client, SpoonTobStatsPlugin plugin, SpoonTobStatsConfig config) {
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    public Dimension render(Graphics2D graphics) {
        if (this.plugin.isFlash() && this.config.flash()) {
            Color flash = graphics.getColor();
            graphics.setColor(new Color(255, 0, 0, 70));
            graphics.fill(new Rectangle(this.client.getCanvas().getSize()));
            graphics.setColor(flash);
            this.timeout++;
            if (this.timeout >= 50) {
                this.timeout = 0;
                this.plugin.setFlash(false);
            }
        }
        return null;
    }
}
