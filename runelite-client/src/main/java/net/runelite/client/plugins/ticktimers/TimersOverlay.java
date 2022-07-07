package net.runelite.client.plugins.ticktimers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class TimersOverlay extends Overlay {
    private static final int TICK_PIXEL_SIZE = 60;

    private static final int BOX_WIDTH = 10;

    private static final int BOX_HEIGHT = 5;

    private final TickTimersPlugin plugin;

    private final TickTimersConfig config;

    private final Client client;

    @Inject
    TimersOverlay(TickTimersPlugin plugin, TickTimersConfig config, Client client) {
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGHEST);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    public Dimension render(Graphics2D graphics) {
        TreeMap<Integer, TreeMap<Integer, Prayer>> tickAttackMap = new TreeMap<>();
        for (NPCContainer npc : this.plugin.getNpcContainers()) {
            if (npc.getNpc() == null)
                continue;
            int ticksLeft = npc.getTicksUntilAttack();
            List<WorldPoint> hitSquares = OverlayUtil.getHitSquares(npc.getNpc().getWorldLocation(), npc.getNpcSize(), 1, false);
            NPCContainer.AttackStyle attackStyle = npc.getAttackStyle();
            if (this.config.showHitSquares() && attackStyle.getName().equals("Melee"))
                for (WorldPoint p : hitSquares)
                    OverlayUtil.drawTiles(graphics, this.client, p, this.client.getLocalPlayer().getWorldLocation(), attackStyle.getColor(), 0, 0, 50);
            if (ticksLeft <= 0)
                continue;
            if (this.config.ignoreNonAttacking() && npc.getNpcInteracting() != this.client.getLocalPlayer() && npc.getMonsterType() != NPCContainer.BossMonsters.GENERAL_GRAARDOR)
                continue;
            if (npc.getMonsterType() == NPCContainer.BossMonsters.GENERAL_GRAARDOR && npc.getNpcInteracting() != this.client.getLocalPlayer())
                attackStyle = NPCContainer.AttackStyle.RANGE;
            String ticksLeftStr = String.valueOf(ticksLeft);
            int font = this.config.fontStyle().getFont();
            boolean shadows = this.config.shadows();
            Color color = (ticksLeft <= 1) ? Color.WHITE : attackStyle.getColor();
            if (!this.config.changeTickColor())
                color = attackStyle.getColor();
            Point canvasPoint = npc.getNpc().getCanvasTextLocation(graphics, ticksLeftStr, 0);
            OverlayUtil.renderTextLocation(graphics, ticksLeftStr, this.config.textSize(), font, color, canvasPoint, shadows, 0);
            if (this.config.showPrayerWidgetHelper() && attackStyle.getPrayer() != null) {
                Rectangle bounds = OverlayUtil.renderPrayerOverlay(graphics, this.client, attackStyle.getPrayer(), color);
                if (bounds != null)
                    renderTextLocation(graphics, ticksLeftStr, 16, this.config.fontStyle().getFont(), color, centerPoint(bounds), shadows);
            }
            if (this.config.guitarHeroMode()) {
                TreeMap<Integer, Prayer> attacks = tickAttackMap.computeIfAbsent(Integer.valueOf(ticksLeft), k -> new TreeMap<>());
                int priority = 999;
                switch (npc.getMonsterType()) {
                    case SERGEANT_STRONGSTACK:
                        priority = 3;
                        break;
                    case SERGEANT_STEELWILL:
                        priority = 1;
                        break;
                    case SERGEANT_GRIMSPIKE:
                        priority = 2;
                        break;
                    case GENERAL_GRAARDOR:
                        priority = 0;
                        break;
                }
                attacks.putIfAbsent(Integer.valueOf(priority), attackStyle.getPrayer());
            }
        }
        if (!tickAttackMap.isEmpty())
            for (Map.Entry<Integer, TreeMap<Integer, Prayer>> tickEntry : tickAttackMap.entrySet()) {
                Map.Entry<Integer, Prayer> attackEntry = ((TreeMap<Integer, Prayer>)tickEntry.getValue()).firstEntry();
                Prayer prayer = attackEntry.getValue();
                if (prayer != null)
                    renderDescendingBoxes(graphics, prayer, ((Integer)tickEntry.getKey()).intValue());
            }
        return null;
    }

    private void renderDescendingBoxes(Graphics2D graphics, Prayer prayer, int tick) {
        Color color = (tick == 1) ? Color.RED : Color.ORANGE;
        Widget prayerWidget = this.client.getWidget(prayer.getWidgetInfo());
        int baseX = (int)prayerWidget.getBounds().getX();
        baseX = (int)(baseX + prayerWidget.getBounds().getWidth() / 2.0D);
        baseX -= 5;
        int baseY = (int)prayerWidget.getBounds().getY() - tick * 60 - 5;
        baseY = (int)(baseY + 60.0D - (this.plugin.getLastTickTime() + 600L - System.currentTimeMillis()) / 600.0D * 60.0D);
        Rectangle boxRectangle = new Rectangle(10, 5);
        boxRectangle.translate(baseX, baseY);
        OverlayUtil.renderFilledPolygon(graphics, boxRectangle, color);
    }

    private void renderTextLocation(Graphics2D graphics, String txtString, int fontSize, int fontStyle, Color fontColor, Point canvasPoint, boolean shadows) {
        graphics.setFont(new Font("Arial", fontStyle, fontSize));
        if (canvasPoint != null) {
            Point canvasCenterPoint = new Point(canvasPoint.getX() - 3, canvasPoint.getY() + 6);
            Point canvasCenterPoint_shadow = new Point(canvasPoint.getX() - 2, canvasPoint.getY() + 7);
            if (shadows)
                OverlayUtil.renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
            OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
        }
    }

    private Point centerPoint(Rectangle rect) {
        int x = (int)(rect.getX() + rect.getWidth() / 2.0D);
        int y = (int)(rect.getY() + rect.getHeight() / 2.0D);
        return new Point(x, y);
    }
}
