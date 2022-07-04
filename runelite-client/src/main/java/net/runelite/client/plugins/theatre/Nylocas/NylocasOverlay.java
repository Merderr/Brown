package net.runelite.client.plugins.theatre.Nylocas;

import com.google.common.collect.ImmutableList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.plugins.theatre.TheatrePlugin;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayUtil;

public class NylocasOverlay extends RoomOverlay {
    @Inject
    private Nylocas nylocas;

    private final List<Point> eastSpawnNorthLocalPoints;

    private final List<Point> eastSpawnSouthLocalPoints;

    private final List<Point> southSpawnEastLocalPoints;

    private final List<Point> southSpawnWestLocalPoints;

    private final List<Point> westSpawnSouthLocalPoints;

    private final List<Point> westSpawnNorthLocalPoints;

    @Inject
    protected NylocasOverlay(TheatreConfig config) {
        super(config);
        this

                .eastSpawnNorthLocalPoints = (List<Point>)(new ImmutableList.Builder()).add(new Point(38, 25)).add(new Point(34, 25)).add(new Point(32, 25)).build();
        this

                .eastSpawnSouthLocalPoints = (List<Point>)(new ImmutableList.Builder()).add(new Point(38, 24)).add(new Point(34, 24)).add(new Point(32, 24)).build();
        this

                .southSpawnEastLocalPoints = (List<Point>)(new ImmutableList.Builder()).add(new Point(24, 9)).add(new Point(24, 14)).add(new Point(24, 16)).build();
        this

                .southSpawnWestLocalPoints = (List<Point>)(new ImmutableList.Builder()).add(new Point(23, 9)).add(new Point(23, 14)).add(new Point(23, 16)).build();
        this

                .westSpawnSouthLocalPoints = (List<Point>)(new ImmutableList.Builder()).add(new Point(9, 24)).add(new Point(13, 24)).add(new Point(15, 24)).build();
        this

                .westSpawnNorthLocalPoints = (List<Point>)(new ImmutableList.Builder()).add(new Point(9, 25)).add(new Point(13, 25)).add(new Point(15, 25)).build();
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.nylocas.isInstanceTimerRunning() && this.nylocas.inRoomRegion(TheatrePlugin.NYLOCAS_REGION) && this.config.nyloInstanceTimer()) {
            Player player = this.client.getLocalPlayer();
            if (player != null) {
                Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
                if (point != null)
                    renderTextLocation(graphics, String.valueOf(this.nylocas.getInstanceTimer()), Color.CYAN, point);
            }
        }
        if (this.nylocas.isNyloBossAlive()) {
            String text = "";
            if (this.config.nyloBossAttackTickCount() && this.nylocas.getNyloBossAttackTickCount() >= 0) {
                text = text + "[A] " + text;
                if (this.config.nyloBossSwitchTickCount() || this.config.nyloBossTotalTickCount())
                    text = text + " : ";
            }
            if (this.config.nyloBossSwitchTickCount() && this.nylocas.getNyloBossSwitchTickCount() >= 0) {
                text = text + "[S] " + text;
                if (this.config.nyloBossTotalTickCount() && this.nylocas.getNyloBossTotalTickCount() >= 0)
                    text = text + " : ";
            }
            if (this.config.nyloBossTotalTickCount() && this.nylocas.getNyloBossTotalTickCount() >= 0)
                text = text + "(" + text + ")";
            Point canvasPoint = this.nylocas.getNyloBossNPC().getCanvasTextLocation(graphics, text, 50);
            renderTextLocation(graphics, text, Color.WHITE, canvasPoint);
        }
        if (this.nylocas.isNyloActive()) {
            if (this.config.nyloWavesHelper()) {
                /*
                String[] nylocasWave = (String[])NylocasWave.wavesHelper.get(Integer.valueOf(this.nylocas.getNyloWave() + 1));
                if (nylocasWave != null) {
                    String eastSpawn = nylocasWave[0];
                    String southSpawn = nylocasWave[1];
                    String westSpawn = nylocasWave[2];
                    String[] eastSpawnSplit = eastSpawn.split("\\|");
                    String[] southSpawnSplit = southSpawn.split("\\|");
                    String[] westSpawnSplit = westSpawn.split("\\|");
                    if (eastSpawnSplit.length > 1) {
                        renderNyloHelperOnWalkup(graphics, eastSpawnSplit[0], this.eastSpawnNorthLocalPoints, "east");
                        renderNyloHelperOnWalkup(graphics, eastSpawnSplit[1], this.eastSpawnSouthLocalPoints, "east");
                    } else {
                        renderNyloHelperOnWalkup(graphics, eastSpawn, this.eastSpawnNorthLocalPoints, "east");
                    }
                    if (southSpawnSplit.length > 1) {
                        renderNyloHelperOnWalkup(graphics, southSpawnSplit[0], this.southSpawnEastLocalPoints, "south");
                        renderNyloHelperOnWalkup(graphics, southSpawnSplit[1], this.southSpawnWestLocalPoints, "south");
                    } else {
                        renderNyloHelperOnWalkup(graphics, southSpawn, this.southSpawnEastLocalPoints, "south");
                    }
                    if (westSpawnSplit.length > 1) {
                        renderNyloHelperOnWalkup(graphics, westSpawnSplit[0], this.westSpawnSouthLocalPoints, "west");
                        renderNyloHelperOnWalkup(graphics, westSpawnSplit[1], this.westSpawnNorthLocalPoints, "west");
                    } else {
                        renderNyloHelperOnWalkup(graphics, westSpawn, this.westSpawnSouthLocalPoints, "west");
                    }
                }*/
            }
            if (this.config.nyloTicksUntilWaves() && !this.nylocas.isNyloBossAlive()) {
                LocalPoint eastPoint = LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(((Player)Objects.<Player>requireNonNull(this.client.getLocalPlayer())).getWorldLocation().getRegionID(), 43, 25, this.client.getLocalPlayer().getWorldLocation().getPlane()));
                LocalPoint southPoint = LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(((Player)Objects.<Player>requireNonNull(this.client.getLocalPlayer())).getWorldLocation().getRegionID(), 25, 6, this.client.getLocalPlayer().getWorldLocation().getPlane()));
                LocalPoint westPoint = LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(((Player)Objects.<Player>requireNonNull(this.client.getLocalPlayer())).getWorldLocation().getRegionID(), 5, 24, this.client.getLocalPlayer().getWorldLocation().getPlane()));
                Polygon southPoly = null;
                Polygon eastPoly = null;
                Polygon westPoly = null;
                if (southPoint != null)
                    southPoly = Perspective.getCanvasTileAreaPoly(this.client, new LocalPoint(southPoint.getX() - 64, southPoint.getY() + 64), 2);
                if (eastPoint != null)
                    eastPoly = Perspective.getCanvasTileAreaPoly(this.client, new LocalPoint(eastPoint.getX() - 64, eastPoint.getY() - 64), 2);
                if (westPoint != null)
                    westPoly = Perspective.getCanvasTileAreaPoly(this.client, new LocalPoint(westPoint.getX() + 64, westPoint.getY() + 64), 2);
                if (eastPoly != null)
                    renderTextLocation(graphics, String.valueOf(this.nylocas.getTicksUntilNextWave()), Color.CYAN, centerPoint(eastPoly.getBounds()));
                if (southPoly != null)
                    renderTextLocation(graphics, String.valueOf(this.nylocas.getTicksUntilNextWave()), Color.CYAN, centerPoint(southPoly.getBounds()));
                if (westPoly != null)
                    renderTextLocation(graphics, String.valueOf(this.nylocas.getTicksUntilNextWave()), Color.CYAN, centerPoint(westPoly.getBounds()));
            }
            if (this.config.nyloPillars()) {
                Map<NPC, Integer> pillars = this.nylocas.getNylocasPillars();
                for (NPC npc : pillars.keySet()) {
                    int health = ((Integer)pillars.get(npc)).intValue();
                    String healthStr = "" + health + "%";
                    WorldPoint p = npc.getWorldLocation();
                    LocalPoint lp = LocalPoint.fromWorld(this.client, p.getX() + 1, p.getY() + 1);
                    double rMod = 130.0D * health / 100.0D;
                    double gMod = 255.0D * health / 100.0D;
                    double bMod = 125.0D * health / 100.0D;
                    Color c = new Color((int)(255.0D - rMod), (int)(0.0D + gMod), (int)(0.0D + bMod));
                    if (lp != null) {
                        Point canvasPoint = Perspective.localToCanvas(this.client, lp, this.client.getPlane(), 65);
                        renderTextLocation(graphics, healthStr, c, canvasPoint);
                    }
                }
            }
            Map<NPC, Integer> npcMap = this.nylocas.getNylocasNpcs();
            for (NPC npc : npcMap.keySet()) {
                int npcSize = npc.getComposition().getSize();
                if (this.config.nyloAggressiveOverlay() && this.nylocas.getAggressiveNylocas().contains(npc) && !npc.isDead())
                    if (this.config.nyloAggressiveOverlayStyle() == TheatreConfig.AGGRESSIVENYLORENDERSTYLE.TILE) {
                        LocalPoint lp = npc.getLocalLocation();
                        if (lp != null) {
                            Polygon poly = getCanvasTileAreaPoly(this.client, lp, npcSize, -25);
                            renderPoly(graphics, Color.RED, poly, 1);
                        }
                    } else if (this.config.nyloAggressiveOverlayStyle() == TheatreConfig.AGGRESSIVENYLORENDERSTYLE.HULL) {
                        Shape objectClickbox = npc.getConvexHull();
                        if (objectClickbox != null) {
                            Color color = Color.RED;
                            graphics.setColor(color);
                            graphics.setStroke(new BasicStroke(2.0F));
                            graphics.draw(objectClickbox);
                            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                            graphics.fill(objectClickbox);
                        }
                    }
                int ticksLeft = ((Integer)npcMap.get(npc)).intValue();
                if (ticksLeft > -1 && ticksLeft <= this.config.nyloExplosionDisplayTicks()) {
                    if (this.config.nyloTimeAlive() && !npc.isDead()) {
                        int ticksAlive = ticksLeft;
                        if (this.config.nyloTimeAliveCountStyle() == TheatreConfig.NYLOTIMEALIVE.COUNTUP)
                            ticksAlive = 52 - ticksLeft;
                        Point textLocation = npc.getCanvasTextLocation(graphics, String.valueOf(ticksAlive), 60);
                        if (textLocation != null)
                            if (this.config.nyloExplosionOverlayStyle() == TheatreConfig.EXPLOSIVENYLORENDERSTYLE.RECOLOR_TICK && this.config
                                    .nyloExplosions() && ticksLeft <= 6) {
                                renderTextLocation(graphics, String.valueOf(ticksAlive), Color.RED, textLocation);
                            } else {
                                renderTextLocation(graphics, String.valueOf(ticksAlive), Color.WHITE, textLocation);
                            }
                    }
                    if (this.config.nyloExplosions() && ticksLeft <= 6)
                        if (this.config.nyloExplosionOverlayStyle() == TheatreConfig.EXPLOSIVENYLORENDERSTYLE.TILE) {
                            LocalPoint lp = npc.getLocalLocation();
                            if (lp != null)
                                renderPoly(graphics, Color.YELLOW, getCanvasTileAreaPoly(this.client, lp, npcSize, -15), 1);
                        }
                }
                String name = npc.getName();
                if (this.config.nyloHighlightOverlay() && !npc.isDead()) {
                    LocalPoint lp = npc.getLocalLocation();
                    if (lp != null) {
                        if (this.config.getHighlightMeleeNylo() && "Nylocas Ischyros".equals(name)) {
                            renderPoly(graphics, new Color(255, 188, 188), Perspective.getCanvasTileAreaPoly(this.client, lp, npcSize), 1);
                            continue;
                        }
                        if (this.config.getHighlightRangeNylo() && "Nylocas Toxobolos".equals(name)) {
                            renderPoly(graphics, Color.GREEN, Perspective.getCanvasTileAreaPoly(this.client, lp, npcSize), 1);
                            continue;
                        }
                        if (this.config.getHighlightMageNylo() && "Nylocas Hagios".equals(name))
                            renderPoly(graphics, Color.CYAN, Perspective.getCanvasTileAreaPoly(this.client, lp, npcSize), 1);
                    }
                }
            }
            if (this.config.bigSplits())
                this.nylocas.getSplitsMap().forEach((lp, ticks) -> {
                    Polygon poly = Perspective.getCanvasTileAreaPoly(this.client, lp, 2);
                    if (poly != null) {
                        if (ticks.intValue() == 1)
                            OverlayUtil.renderPolygon(graphics, poly, this.config.getBigSplitsTileColor1());
                        if (ticks.intValue() == 2)
                            OverlayUtil.renderPolygon(graphics, poly, this.config.getBigSplitsTileColor2());
                        if (ticks.intValue() >= 3)
                            OverlayUtil.renderPolygon(graphics, poly, this.config.getBigSplitsHighlightColor());
                    }
                    Point textLocation = Perspective.getCanvasTextLocation(this.client, graphics, lp, "#", 0);
                    if (textLocation != null) {
                        if (ticks.intValue() == 1)
                            OverlayUtil.renderTextLocation(graphics, textLocation, Integer.toString(ticks.intValue()), this.config.getBigSplitsTextColor1());
                        if (ticks.intValue() == 2)
                            OverlayUtil.renderTextLocation(graphics, textLocation, Integer.toString(ticks.intValue()), this.config.getBigSplitsTextColor2());
                        if (ticks.intValue() >= 3)
                            OverlayUtil.renderTextLocation(graphics, textLocation, Integer.toString(ticks.intValue()), Color.WHITE);
                    }
                });
        }
        return null;
    }

    private void renderNyloHelperOnWalkup(Graphics2D graphics, String nyloHelperString, List<Point> pointArray, String direction) {
        /*if (pointArray.isEmpty())
            return;
        String[] nyloSpawnSplitCsv = nyloHelperString.split("-");
        if (nyloSpawnSplitCsv.length > 1) {
            for (int i = 0; i < nyloSpawnSplitCsv.length; i++)
                drawPoly(graphics, nyloSpawnSplitCsv[i], direction, LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(((Player)Objects.<Player>requireNonNull(this.client.getLocalPlayer())).getWorldLocation().getRegionID(), ((Point)pointArray.get(i)).getX(), ((Point)pointArray.get(i)).getY(), this.client.getLocalPlayer().getWorldLocation().getPlane())));
        } else if (!nyloHelperString.isBlank()) {
            drawPoly(graphics, nyloHelperString, direction, LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(((Player)Objects.<Player>requireNonNull(this.client.getLocalPlayer())).getWorldLocation().getRegionID(), ((Point)pointArray.get(0)).getX(), ((Point)pointArray.get(0)).getY(), this.client.getLocalPlayer().getWorldLocation().getPlane())));
        }*/
    }

    private void drawPoly(Graphics2D graphics, String nyloType, String direction, LocalPoint localPoint) {
        Polygon poly = null;
        if (nyloType.equals("mage") || nyloType.equals("melee") || nyloType.equals("range")) {
            poly = Perspective.getCanvasTilePoly(this.client, localPoint);
        } else {
            LocalPoint localPointBig = null;
            switch (direction) {
                case "east":
                    localPointBig = new LocalPoint(localPoint.getX() - 64, localPoint.getY() - 64);
                    break;
                case "west":
                    localPointBig = new LocalPoint(localPoint.getX() + 64, localPoint.getY() + 64);
                    break;
                case "south":
                    localPointBig = new LocalPoint(localPoint.getX() - 64, localPoint.getY() + 64);
                    break;
            }
            if (localPointBig != null)
                poly = Perspective.getCanvasTileAreaPoly(this.client, localPointBig, 2);
        }
        if (poly != null) {
            renderPolyWithFillAlpha(graphics, getColor(nyloType), poly, 2, 60);
            renderTextLocation(graphics, String.valueOf(this.nylocas.getNyloWave() + 1), Color.YELLOW, centerPoint(poly.getBounds()));
        }
    }

    private Point centerPoint(Rectangle rect) {
        int x = (int)(rect.getX() + rect.getWidth() / 2.0D);
        int y = (int)(rect.getY() + rect.getHeight() / 2.0D);
        return new Point(x, y);
    }

    private Color getColor(String nyloType) {
        if (nyloType.equalsIgnoreCase("melee"))
            return Color.BLACK;
        if (nyloType.equalsIgnoreCase("range"))
            return Color.GREEN;
        if (nyloType.equalsIgnoreCase("mage"))
            return Color.CYAN;
        return Color.BLACK;
    }
}
