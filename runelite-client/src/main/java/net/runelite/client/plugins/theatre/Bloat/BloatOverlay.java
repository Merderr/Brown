package net.runelite.client.plugins.theatre.Bloat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayLayer;

public class BloatOverlay extends RoomOverlay {
    @Inject
    private Bloat bloat;

    @Inject
    protected BloatOverlay(TheatreConfig config) {
        super(config);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.bloat.isBloatActive()) {
            if (this.config.bloatIndicator())
                renderPoly(graphics, this.bloat.getBloatStateColor(), this.bloat.getBloatTilePoly(), 2);
            if (this.config.bloatTickCounter()) {
                NPC boss = this.bloat.getBloatNPC();
                int tick = this.bloat.getBloatTickCount();
                String ticksCounted = String.valueOf(tick);
                Point canvasPoint = boss.getCanvasTextLocation(graphics, ticksCounted, 50);
                if (this.bloat.getBloatState() > 1 && this.bloat.getBloatState() < 4 && this.config.BloatTickCountStyle() == TheatreConfig.BLOATTIMEDOWN.COUNTDOWN) {
                    renderTextLocation(graphics, String.valueOf(33 - this.bloat.getBloatDownCount()), Color.WHITE, canvasPoint);
                } else {
                    renderTextLocation(graphics, ticksCounted, Color.WHITE, canvasPoint);
                }
            }
            if (this.config.bloatHands())
                for (WorldPoint point : this.bloat.getBloatHands().keySet())
                    drawTile(graphics, point, this.config.bloatHandsColor(), this.config.bloatHandsWidth(), 255, 10);
        }
        return null;
    }
}
