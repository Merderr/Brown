package net.runelite.client.plugins.a_Qol_shit;

import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

public class RemindersOverlay extends Overlay {
    private final ToBQoLImprovementsConfig config;
    private final ToBQoLImprovementsPlugin plugin;

    @Inject
    private RemindersOverlay(ToBQoLImprovementsConfig config, ToBQoLImprovementsPlugin plugin)
    {
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        TileObject chest = plugin.getLootChest();
        OverlayUtil.renderTileOverlay(graphics, chest, "Fuck OpenOsrs - Merderr", Color.BLUE);
        if (config.lootReminder() && plugin.isInVerSinhaza() && plugin.getLootChest() != null && plugin.isChestHasLoot())
        {
            Shape poly = plugin.getLootChest().getConvexHull();
            if (poly != null)
            {
                OverlayUtil.renderPolygon(graphics, poly, Color.RED);
            }
        }
        return null;
    }


}

