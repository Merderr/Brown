package net.runelite.client.plugins.theatre.prayer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;
import java.util.Queue;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.theatre.TheatreConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public abstract class TheatrePrayerOverlay extends Overlay {
    private static final int TICK_PIXEL_SIZE = 60;

    private static final int BOX_WIDTH = 10;

    private static final int BOX_HEIGHT = 5;

    private final TheatreConfig config;

    private final Client client;

    protected TheatreConfig getConfig() {
        return this.config;
    }

    @Inject
    protected TheatrePrayerOverlay(Client client, TheatreConfig config) {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
    }

    public Dimension render(Graphics2D graphics) {
        Widget meleePrayerWidget = this.client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);
        Widget rangePrayerWidget = this.client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
        Widget magicPrayerWidget = this.client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
        boolean prayerWidgetHidden = (meleePrayerWidget == null || rangePrayerWidget == null || magicPrayerWidget == null || meleePrayerWidget.isHidden() || rangePrayerWidget.isHidden() || magicPrayerWidget.isHidden());
        if (this.config.prayerHelper() && isEnabled() && (!prayerWidgetHidden || this.config.alwaysShowPrayerHelper())) {
            renderPrayerIconOverlay(graphics);
            if (this.config.descendingBoxes())
                renderDescendingBoxes(graphics);
        }
        return null;
    }

    private void renderDescendingBoxes(Graphics2D graphics) {
        Map<Integer, TheatreUpcomingAttack> tickPriorityMap = TheatrePrayerUtil.getTickPriorityMap(getAttackQueue());
        getAttackQueue().forEach(attack -> {
            int tick = attack.getTicksUntil();
            Color color = (tick == 1) ? this.config.prayerColorDanger() : this.config.prayerColor();
            Widget prayerWidget = this.client.getWidget(attack.getPrayer().getWidgetInfo());
            if (prayerWidget == null)
                return;
            int baseX = (int)prayerWidget.getBounds().getX();
            baseX = (int)(baseX + prayerWidget.getBounds().getWidth() / 2.0D);
            baseX -= 5;
            int baseY = (int)prayerWidget.getBounds().getY() - tick * 60 - 5;
            baseY = (int)(baseY + 60.0D - (getLastTick() + 600L - System.currentTimeMillis()) / 600.0D * 60.0D);
            Rectangle boxRectangle = new Rectangle(10, 5);
            boxRectangle.translate(baseX, baseY);
            if (attack.getPrayer().equals(((TheatreUpcomingAttack)tickPriorityMap.get(Integer.valueOf(attack.getTicksUntil()))).getPrayer())) {
                OverlayUtil.renderPolygon(graphics, boxRectangle, color, color, new BasicStroke(2.0F));
            } else if (this.config.indicateNonPriorityDescendingBoxes()) {
                OverlayUtil.renderPolygon(graphics, boxRectangle, color, new Color(0, 0, 0, 0), new BasicStroke(2.0F));
            }
        });
    }

    private void renderPrayerIconOverlay(Graphics2D graphics) {
        TheatreUpcomingAttack attack = getAttackQueue().peek();
        if (attack == null)
            return;
        if (!this.client.isPrayerActive(attack.getPrayer())) {
            Widget prayerWidget = this.client.getWidget(attack.getPrayer().getWidgetInfo());
            if (prayerWidget == null)
                return;
            Rectangle prayerRectangle = new Rectangle((int)prayerWidget.getBounds().getWidth(), (int)prayerWidget.getBounds().getHeight());
            prayerRectangle.translate((int)prayerWidget.getBounds().getX(), (int)prayerWidget.getBounds().getY());
            OverlayUtil.renderPolygon(graphics, prayerRectangle, this.config.prayerColorDanger());
        }
    }

    protected abstract Queue<TheatreUpcomingAttack> getAttackQueue();

    protected abstract long getLastTick();

    protected abstract boolean isEnabled();
}
