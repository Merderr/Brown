package net.runelite.client.plugins.spoontobstats;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class SpoonTobStatsOverlay extends OverlayPanel {
    private final Client client;

    private final SpoonTobStatsPlugin plugin;

    private final SpoonTobStatsConfig config;

    @Inject
    private SpoonTobStatsOverlay(Client client, SpoonTobStatsPlugin plugin, SpoonTobStatsConfig config) {
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.timerOverlay() && this.plugin.isTobInside()) {
            if (!this.config.fontName().equals(""))
                graphics.setFont(new Font(this.config.fontName(), (this.config.fontWeight()).weight, this.config.fontSize()));
            LineComponent lineComponent = null;
            this.panelComponent.getChildren().clear();
            for (String room : this.plugin.getRoom().keySet()) {
                lineComponent = LineComponent.builder().left(room).build();
                if (this.plugin.getTime().get(room) != null) {
                    lineComponent.setRightColor(Color.GREEN);
                    String time = this.plugin.formatTime(((Integer)this.plugin.getTime().get(room)).intValue());
                    if (this.plugin.getPhaseSplit().get(room) != null) {
                        lineComponent.setRight("(" + this.plugin.formatTime(((Integer)this.plugin.getPhaseSplit().get(room)).intValue()) + ") " + time);
                        continue;
                    }
                    lineComponent.setRight(time);
                    continue;
                }
                String current = this.plugin.formatTime(this.client.getTickCount() - ((Integer)this.plugin.getRoom().get(room)).intValue());
                if (!this.plugin.getPhase().isEmpty()) {
                    String phase = this.plugin.getPhase().getLast();
                    if (this.plugin.getPhaseSplit().get(phase) != null) {
                        lineComponent.setRight("(" + this.plugin.formatTime(((Integer)this.plugin.getPhaseSplit().get(phase)).intValue()) + ") " + current);
                        continue;
                    }
                    lineComponent.setRight("(" + this.plugin.formatTime(((Integer)this.plugin.getPhaseTime().get(phase)).intValue()) + ") " + current);
                    continue;
                }
                lineComponent.setRight(current);
            }
            if (lineComponent != null)
                this.panelComponent.getChildren().add(lineComponent);
            if (!this.config.simpleOverlay())
                for (String phase : this.plugin.getPhase()) {
                    String phaseTime = this.plugin.formatTime(((Integer)this.plugin.getPhaseTime().get(phase)).intValue());
                    if (this.plugin.getPhaseSplit().get(phase) != null) {
                        this.panelComponent.getChildren().add(LineComponent.builder().left(phase).right(phaseTime + " (" + phaseTime + ")").build());
                        continue;
                    }
                    this.panelComponent.getChildren().add(LineComponent.builder().left(phase).right(phaseTime).build());
                }
            return this.panelComponent.render(graphics);
        }
        return null;
    }
}

