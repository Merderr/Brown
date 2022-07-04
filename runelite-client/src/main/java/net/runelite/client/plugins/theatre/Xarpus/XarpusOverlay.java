package net.runelite.client.plugins.theatre.Xarpus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import javax.inject.Inject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.theatre.Direction;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.ui.overlay.OverlayLayer;
import org.apache.commons.lang3.tuple.Pair;

public class XarpusOverlay extends RoomOverlay {
    @Inject
    private Xarpus xarpus;

    private static final Function<WorldPoint, Point[]> getNEBoxPoints;

    private static final Function<WorldPoint, Point[]> getNWBoxPoints;

    private static final Function<WorldPoint, Point[]> getSEBoxPoints;

    private static final Function<WorldPoint, Point[]> getSWBoxPoints;

    private static final Function<WorldPoint, Point[]> getNEMeleePoints;

    private static final Function<WorldPoint, Point[]> getNWMeleePoints;

    private static final Function<WorldPoint, Point[]> getSEMeleePoints;

    private static final Function<WorldPoint, Point[]> getSWMeleePoints;

    @Inject
    protected XarpusOverlay(TheatreConfig config) {
        super(config);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    static {
        getNEBoxPoints = (p -> new Point[] { new Point(p.getX(), p.getY()), new Point(p.getX(), p.getY() + 8), new Point(p.getX() + 8, p.getY() + 8), new Point(p.getX() + 8, p.getY()) });
        getNWBoxPoints = (p -> new Point[] { new Point(p.getX() - 8, p.getY()), new Point(p.getX() - 8, p.getY() + 8), new Point(p.getX(), p.getY() + 8), new Point(p.getX(), p.getY()) });
        getSEBoxPoints = (p -> new Point[] { new Point(p.getX(), p.getY() - 8), new Point(p.getX(), p.getY()), new Point(p.getX() + 8, p.getY()), new Point(p.getX() + 8, p.getY() - 8) });
        getSWBoxPoints = (p -> new Point[] { new Point(p.getX() - 8, p.getY() - 8), new Point(p.getX() - 8, p.getY()), new Point(p.getX(), p.getY()), new Point(p.getX(), p.getY() - 8) });
        getNEMeleePoints = (p -> new Point[] { new Point(p.getX() + 4, p.getY() + 4), new Point(p.getX(), p.getY() + 4), new Point(p.getX(), p.getY() + 3), new Point(p.getX() + 3, p.getY() + 3), new Point(p.getX() + 3, p.getY()), new Point(p.getX() + 4, p.getY()) });
        getNWMeleePoints = (p -> new Point[] { new Point(p.getX() - 4, p.getY() + 4), new Point(p.getX() - 4, p.getY()), new Point(p.getX() - 3, p.getY()), new Point(p.getX() - 3, p.getY() + 3), new Point(p.getX(), p.getY() + 3), new Point(p.getX(), p.getY() + 4) });
        getSEMeleePoints = (p -> new Point[] { new Point(p.getX() + 4, p.getY() - 4), new Point(p.getX() + 4, p.getY()), new Point(p.getX() + 3, p.getY()), new Point(p.getX() + 3, p.getY() - 3), new Point(p.getX(), p.getY() - 3), new Point(p.getX(), p.getY() - 4) });
        getSWMeleePoints = (p -> new Point[] { new Point(p.getX() - 4, p.getY() - 4), new Point(p.getX(), p.getY() - 4), new Point(p.getX(), p.getY() - 3), new Point(p.getX() - 3, p.getY() - 3), new Point(p.getX() - 3, p.getY()), new Point(p.getX() - 4, p.getY()) });
    }

    public Dimension render(Graphics2D graphics) {
        if (this.xarpus.isInstanceTimerRunning() && !this.xarpus.isExhumedSpawned() && this.xarpus.inRoomRegion(TheatrePlugin.XARPUS_REGION) && this.config.xarpusInstanceTimer()) {
            Player player = this.client.getLocalPlayer();
            if (player != null) {
                Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
                if (point != null)
                    renderTextLocation(graphics, String.valueOf(this.xarpus.getInstanceTimer()), Color.CYAN, point);
            }
        }
        if (this.xarpus.isXarpusActive()) {
            NPC boss = this.xarpus.getXarpusNPC();
            if ((this.config.xarpusTickP2() && (boss.getId() == 8340 || boss.getId() == 10768 || boss.getId() == 10772)) || (this.config
                    .xarpusTickP3() && (boss.getId() == 8341 || boss.getId() == 10769 || boss.getId() == 10773))) {
                int tick = this.xarpus.getXarpusTicksUntilAttack();
                String ticksLeftStr = String.valueOf(tick);
                Point canvasPoint = boss.getCanvasTextLocation(graphics, ticksLeftStr, 130);
                renderTextLocation(graphics, ticksLeftStr, Color.WHITE, canvasPoint);
            }
            if ((this.config.xarpusExhumed() || this.config.xarpusExhumedTick()) && (boss.getId() == 8339 || boss.getId() == 10767 || boss.getId() == 10771))
                if (!this.xarpus.getXarpusExhumeds().isEmpty()) {
                    Collection<Pair<GroundObject, Integer>> exhumeds = this.xarpus.getXarpusExhumeds().values();
                    exhumeds.forEach(p -> {
                        GroundObject o = (GroundObject)p.getLeft();
                        int ticks = ((Integer)p.getRight()).intValue();
                        if (this.config.xarpusExhumed()) {
                            Polygon poly = o.getCanvasTilePoly();
                            if (poly != null) {
                                graphics.setColor(new Color(0, 255, 0, 130));
                                graphics.setStroke(new BasicStroke(1.0F));
                                graphics.draw(poly);
                            }
                        }
                        if (this.config.xarpusExhumedTick()) {
                            String count = Integer.toString(ticks);
                            LocalPoint lp = o.getLocalLocation();
                            Point point = Perspective.getCanvasTextLocation(this.client, graphics, lp, count, 0);
                            if (point != null)
                                renderTextLocation(graphics, count, Color.WHITE, point);
                        }
                    });
                }
            if (this.config.xarpusLineOfSight() != TheatreConfig.XARPUS_LINE_OF_SIGHT.OFF)
                renderLineOfSightPolygon(graphics);
        }
        return null;
    }

    private void renderLineOfSightPolygon(Graphics2D graphics) {
        NPC xarpusNpc = this.xarpus.getXarpusNPC();
        if (xarpusNpc != null && (xarpusNpc.getId() == 8340 || xarpusNpc.getId() == 10768 || xarpusNpc.getId() == 10772) && !xarpusNpc.isDead() && this.xarpus.isPostScreech()) {
            WorldPoint xarpusWorldPoint = WorldPoint.fromLocal(this.client, xarpusNpc.getLocalLocation());
            Direction dir = Direction.getPreciseDirection(xarpusNpc.getOrientation());
            if (dir != null) {
                Point[] points;
                boolean markMeleeTiles = (this.config.xarpusLineOfSight() == TheatreConfig.XARPUS_LINE_OF_SIGHT.MELEE_TILES);
                switch (dir) {
                    case NORTHEAST:
                        points = markMeleeTiles ? getNEMeleePoints.apply(xarpusWorldPoint) : getNEBoxPoints.apply(xarpusWorldPoint);
                        break;
                    case NORTHWEST:
                        points = markMeleeTiles ? getNWMeleePoints.apply(xarpusWorldPoint) : getNWBoxPoints.apply(xarpusWorldPoint);
                        break;
                    case SOUTHEAST:
                        points = markMeleeTiles ? getSEMeleePoints.apply(xarpusWorldPoint) : getSEBoxPoints.apply(xarpusWorldPoint);
                        break;
                    case SOUTHWEST:
                        points = markMeleeTiles ? getSWMeleePoints.apply(xarpusWorldPoint) : getSWBoxPoints.apply(xarpusWorldPoint);
                        break;
                    default:
                        return;
                }
                Polygon poly = new Polygon();
                Point[] dangerousPolygonPoints = points;
                int dangerousPolygonPointsLength = points.length;
                Arrays.<Point>stream(dangerousPolygonPoints, 0, dangerousPolygonPointsLength)
                        .map(point -> localToCanvas(dir, point.getX(), point.getY()))
                        .filter(Objects::nonNull)
                        .forEach(p -> poly.addPoint(p.getX(), p.getY()));
                renderPoly(graphics, this.config.xarpusLineOfSightColor(), poly);
            }
        }
    }

    private Point localToCanvas(Direction dir, int px, int py) {
        LocalPoint lp = LocalPoint.fromWorld(this.client, px, py);
        int x = lp.getX();
        int y = lp.getY();
        int s = 64;
        switch (dir) {
            case NORTHEAST:
                return Perspective.localToCanvas(this.client, new LocalPoint(x - s, y - s), this.client.getPlane());
            case NORTHWEST:
                return Perspective.localToCanvas(this.client, new LocalPoint(x + s, y - s), this.client.getPlane());
            case SOUTHEAST:
                return Perspective.localToCanvas(this.client, new LocalPoint(x - s, y + s), this.client.getPlane());
            case SOUTHWEST:
                return Perspective.localToCanvas(this.client, new LocalPoint(x + s, y + s), this.client.getPlane());
        }
        return null;
    }
}
