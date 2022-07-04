package net.runelite.client.plugins.theatre.Verzik;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Iterator;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.theatre.RoomOverlay;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class VerzikOverlay extends RoomOverlay {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

    private static final int VERZIK_GREEN_BALL = 1598;

    private static final int VERZIK_LIGHTNING_BALL = 1585;

    @Inject
    private Verzik verzik;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    protected VerzikOverlay(TheatreConfig config, SpriteManager spriteManager) {
        super(config);
        this.spriteManager = spriteManager;
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.verzik.isVerzikActive()) {
            if (this.config.verzikTileOverlay()) {
                int size = 1;
                NPCComposition composition = this.verzik.getVerzikNPC().getTransformedComposition();
                if (composition != null)
                    size = composition.getSize();
                LocalPoint lp = LocalPoint.fromWorld(this.client, this.verzik.getVerzikNPC().getWorldLocation());
                if (lp != null) {
                    Polygon tilePoly = getCanvasTileAreaPoly(this.client, lp, size, false);
                    if (tilePoly != null)
                        if (this.verzik.isVerzikEnraged()) {
                            renderPoly(graphics, new Color(255, 110, 90), tilePoly);
                        } else {
                            renderPoly(graphics, new Color(255, 110, 230), tilePoly);
                        }
                }
            }
            String tick_text = "";
            if (this.config.verzikAttackCounter() && this.verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS) {
                tick_text = tick_text + "[A] " + tick_text;
                if (this.config.verzikAutosTick() || this.config.verzikTotalTickCounter())
                    tick_text = tick_text + " : ";
            }
            if (this.config.verzikAutosTick() && this.verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS) {
                tick_text = tick_text + tick_text;
                if (this.config.verzikTotalTickCounter())
                    tick_text = tick_text + " : ";
            }
            if (this.config.verzikTotalTickCounter())
                tick_text = tick_text + "(" + tick_text + ")";
            Point canvasPoint = this.verzik.getVerzikNPC().getCanvasTextLocation(graphics, tick_text, 60);
            if (canvasPoint != null) {
                Color col = this.verzik.verzikSpecialWarningColor();
                renderTextLocation(graphics, tick_text, col, canvasPoint);
            }
            if (this.verzik.getVerzikPhase() == Verzik.Phase.PHASE2) {
                if (this.config.verzikProjectiles()) {
                    Iterator<WorldPoint> iterator = this.verzik.getVerzikRangeProjectiles().values().iterator();
                    while (iterator.hasNext())
                        drawTile(graphics, iterator.next(), this.config.verzikProjectilesColor(), 1, 255, 20);
                }
                if (this.config.verzikReds()) {
                    this.verzik.getVerzikReds().forEach((k, v) -> {
                        int v_health = ((Integer)v.getValue()).intValue();
                        int v_healthRation = ((Integer)v.getKey()).intValue();
                        if (k.getName() != null && k.getHealthScale() > 0) {
                            v_health = k.getHealthScale();
                            v_healthRation = Math.min(v_healthRation, k.getHealthRatio());
                        }
                        float percentage = v_healthRation / v_health * 100.0F;
                        Point textLocation = k.getCanvasTextLocation(graphics, String.valueOf(DECIMAL_FORMAT.format(percentage)), 80);
                        if (textLocation != null)
                            renderTextLocation(graphics, String.valueOf(DECIMAL_FORMAT.format(percentage)), Color.WHITE, textLocation);
                    });
                    NPC[] reds = (NPC[])this.verzik.getVerzikReds().keySet().toArray((Object[])new NPC[0]);
                    for (NPC npc : reds) {
                        if (npc.getName() != null && npc.getHealthScale() > 0 && npc.getHealthRatio() < 100) {
                            MutablePair mutablePair = new MutablePair(Integer.valueOf(npc.getHealthRatio()), Integer.valueOf(npc.getHealthScale()));
                            if (this.verzik.getVerzikReds().containsKey(npc))
                                this.verzik.getVerzikReds().put(npc, mutablePair);
                        }
                    }
                }
                if (this.verzik.getVerzikPhase() == Verzik.Phase.PHASE2 && this.verzik.getVerzikNPC() != null && this.config.lightningAttackHelper())
                    if (this.verzik.getVerzikLightningAttacks() == 0) {
                        BufferedImage lightningIcon = this.spriteManager.getSprite(558, 0);
                        Point imageLocation = this.verzik.getVerzikNPC().getCanvasImageLocation(lightningIcon, 200);
                        if (imageLocation != null)
                            OverlayUtil.renderImageLocation(graphics, imageLocation, lightningIcon);
                    } else {
                        String attacksLeft = Integer.toString(this.verzik.getVerzikLightningAttacks());
                        Point imageLocation = Perspective.getCanvasTextLocation(this.client, graphics, this.verzik.getVerzikNPC().getLocalLocation(), attacksLeft, 200);
                        renderTextLocation(graphics, attacksLeft, Color.WHITE, imageLocation);
                    }
                if (this.config.lightningAttackTick())
                    this.client.getProjectiles().forEach(p -> {
                        Actor getInteracting = p.getInteracting();
                        if (p.getId() == 1585) {
                            Player localPlayer = this.client.getLocalPlayer();
                            if (getInteracting != null && getInteracting == localPlayer) {
                                Point point = getProjectilePoint(p);
                                if (point != null) {
                                    Point textLocation = new Point(point.getX(), point.getY());
                                    renderTextLocation(graphics, Integer.toString(p.getRemainingCycles() / 30), Color.ORANGE, textLocation);
                                }
                            }
                        }
                    });
                if (this.verzik.isHM() && this.config.verzikPoisonTileHighlight())
                    this.verzik.getVerzikPoisonTiles()
                            .stream()
                            .filter(VerzikPoisonTile::shouldHighlight)
                            .forEach(tile -> drawTile(graphics, tile.getTile(), this.config.verzikPoisonTileHighlightColor(), 1, 255, 20));
            }
            if (this.verzik.getVerzikPhase() == Verzik.Phase.PHASE3) {
                if (this.config.verzikDisplayTank())
                    if (this.verzik.getVerzikNPC().getInteracting() != null) {
                        Polygon tilePoly = this.verzik.getVerzikNPC().getInteracting().getCanvasTilePoly();
                        if (tilePoly != null)
                            renderPoly(graphics, Color.LIGHT_GRAY, tilePoly);
                    }
                if (this.config.verzikTornado() && (!this.config.verzikPersonalTornadoOnly() || (this.config.verzikPersonalTornadoOnly() && this.verzik.getVerzikLocalTornado() != null)))
                    this.verzik.getVerzikTornadoes().forEach(k -> {
                        if (k.getCurrentPosition() != null)
                            drawTile(graphics, k.getCurrentPosition(), this.config.verzikTornadoColor(), 1, 120, 10);
                        if (k.getLastPosition() != null)
                            drawTile(graphics, k.getLastPosition(), this.config.verzikTornadoColor(), 2, 180, 20);
                    });
                if (this.config.verzikYellows())
                    if (this.verzik.getVerzikYellows() > 0) {
                        String text = Integer.toString(this.verzik.getVerzikYellows());
                        for (GraphicsObject object : this.client.getGraphicsObjects()) {
                            if (object.getId() == 1595) {
                                drawTile(graphics, WorldPoint.fromLocal(this.client, object.getLocation()), Color.YELLOW, 1, 255, 0);
                                LocalPoint lp = object.getLocation();
                                Point point = Perspective.getCanvasTextLocation(this.client, graphics, lp, text, 0);
                                renderTextLocation(graphics, text, Color.WHITE, point);
                            }
                        }
                    }
                if (this.config.verzikGreenBall() || this.config.verzikGreenBallTick())
                    for (Projectile p : this.client.getProjectiles()) {
                        if (p.getId() == 1598) {
                            if (this.config.verzikGreenBallTick()) {
                                Point point = getProjectilePoint(p);
                                if (point != null) {
                                    Point textLocation = new Point(point.getX(), point.getY());
                                    renderTextLocation(graphics, Integer.toString(p.getRemainingCycles() / 30), Color.GREEN, textLocation);
                                }
                            }
                            if (this.config.verzikGreenBall()) {
                                Polygon tilePoly;
                                if (this.config.verzikGreenBallMarker() == TheatreConfig.VERZIKBALLTILE.TILE) {
                                    tilePoly = p.getInteracting().getCanvasTilePoly();
                                } else {
                                    tilePoly = getCanvasTileAreaPoly(this.client, p.getInteracting().getLocalLocation(), 3, true);
                                }
                                if (tilePoly != null)
                                    renderPoly(graphics, this.config.verzikGreenBallColor(), tilePoly);
                            }
                        }
                    }
            }
            if (this.verzik.getVerzikPhase() == Verzik.Phase.PHASE2 || this.verzik.getVerzikPhase() == Verzik.Phase.PHASE3)
                if (this.config.verzikNyloPersonalWarning() || this.config.verzikNyloOtherWarning())
                    this.verzik.getVerzikAggros().forEach(k -> {
                        if (k.getInteracting() != null && !k.isDead())
                            if ((this.config.verzikNyloPersonalWarning() && k.getInteracting() == this.client.getLocalPlayer()) || (this.config.verzikNyloOtherWarning() && k.getInteracting() != this.client.getLocalPlayer())) {
                                Color color = Color.LIGHT_GRAY;
                                if (k.getInteracting() == this.client.getLocalPlayer())
                                    color = Color.YELLOW;
                                Point textLocation = k.getCanvasTextLocation(graphics, k.getInteracting().getName(), 80);
                                if (textLocation != null)
                                    OverlayUtil.renderTextLocation(graphics, textLocation, k.getInteracting().getName(), color);
                                if (this.config.verzikNyloExplodeAOE()) {
                                    int size = 1;
                                    int thick_size = 1;
                                    NPCComposition composition = k.getComposition();
                                    if (composition != null)
                                        size = composition.getSize() + 2 * thick_size;
                                    LocalPoint lp = LocalPoint.fromWorld(this.client, k.getWorldLocation());
                                    if (lp != null) {
                                        lp = new LocalPoint(lp.getX() - thick_size * 128, lp.getY() - thick_size * 128);
                                        Polygon tilePoly = getCanvasTileAreaPoly(this.client, lp, size, false);
                                        if (tilePoly != null)
                                            renderPoly(graphics, color, tilePoly);
                                    }
                                }
                            }
                    });
        }
        return null;
    }

    private Point getProjectilePoint(Projectile p) {
        int x = (int)p.getX();
        int y = (int)p.getY();
        int z = (int)p.getZ();
        return Perspective.localToCanvas(this.client, new LocalPoint(x, y), 0, Perspective.getTileHeight(this.client, new LocalPoint(x, y), p.getFloor()) - z);
    }
}
