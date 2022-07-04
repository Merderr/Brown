package net.runelite.client.plugins.theatre.Sotetseg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPriority;

public class SotetsegOverlay extends RoomOverlay {
    @Inject
    private Sotetseg sotetseg;

    @Inject
    protected SotetsegOverlay(TheatreConfig config) {
        super(config);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.sotetseg.isSotetsegActive()) {
            if (this.config.sotetsegAutoAttacksTicks()) {
                int tick = this.sotetseg.getSotetsegTickCount();
                if (tick >= 0) {
                    NPC boss = this.sotetseg.getSotetsegNPC();
                    String ticksCounted = String.valueOf(tick);
                    Point canvasPoint = boss.getCanvasTextLocation(graphics, ticksCounted, 50);
                    renderTextLocation(graphics, ticksCounted, Color.WHITE, canvasPoint);
                }
            }
            if (this.config.sotetsegAttackCounter()) {
                int attack = this.sotetseg.getAttacksLeft();
                if (attack >= 0) {
                    NPC boss = this.sotetseg.getSotetsegNPC();
                    String attacksCounted = String.valueOf(this.sotetseg.getAttacksLeft());
                    Point canvasPoint = boss.getCanvasTextLocation(graphics, attacksCounted, 250);
                    renderTextLocation(graphics, attacksCounted, Color.YELLOW, canvasPoint);
                }
            }
            if (this.config.sotetsegOrbAttacksTicks() || this.config.sotetsegBigOrbTicks())
                for (Projectile p : this.client.getProjectiles()) {
                    int id = p.getId();
                    Point point = Perspective.localToCanvas(this.client, new LocalPoint((int)p.getX(), (int)p.getY()), 0, Perspective.getTileHeight(this.client, new LocalPoint((int)p.getX(), (int)p.getY()), p.getFloor()) - (int)p.getZ());
                    if (point == null)
                        continue;
                    if (p.getInteracting() == this.client.getLocalPlayer() && (id == 1606 || id == 1607) && this.config.sotetsegOrbAttacksTicks())
                        renderTextLocation(graphics, ((id == 1606) ? "M" : "R") + ((id == 1606) ? "M" : "R"), (id == 1606) ? Color.CYAN : Color.GREEN, point);
                    if (id == 1604 && this.config.sotetsegBigOrbTicks()) {
                        renderTextLocation(graphics, String.valueOf(p.getRemainingCycles() / 30), this.config.sotetsegBigOrbTickColor(), point);
                        renderPoly(graphics, this.config.sotetsegBigOrbTileColor(), p.getInteracting().getCanvasTilePoly());
                    }
                }
            if (this.config.sotetsegMaze()) {
                int counter = 1;
                for (Point p : this.sotetseg.getRedTiles()) {
                    WorldPoint wp = this.sotetseg.worldPointFromMazePoint(p);
                    drawTile(graphics, wp, Color.WHITE, 1, 255, 0);
                    LocalPoint lp = LocalPoint.fromWorld(this.client, wp);
                    if (lp != null && !this.sotetseg.isWasInUnderWorld()) {
                        Point textPoint = Perspective.getCanvasTextLocation(this.client, graphics, lp, String.valueOf(counter), 0);
                        if (textPoint != null)
                            renderTextLocation(graphics, String.valueOf(counter), Color.WHITE, textPoint);
                    }
                    counter++;
                }
                for (Point p : this.sotetseg.getGreenTiles()) {
                    WorldPoint wp = this.sotetseg.worldPointFromMazePoint(p);
                    drawTile(graphics, wp, Color.GREEN, 1, 255, 0);
                }
            }
        }
        return null;
    }
}
