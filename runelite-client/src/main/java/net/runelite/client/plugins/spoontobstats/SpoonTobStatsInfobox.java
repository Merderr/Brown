package net.runelite.client.plugins.spoontobstats;

import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import org.apache.commons.lang3.StringUtils;

public class SpoonTobStatsInfobox extends InfoBox {
    private final String room;

    private final String time;

    private final String percent;

    private final String damage;

    private final String splits;

    private final String healed;

    private final SpoonTobStatsConfig config;

    public String getRoom() {
        return this.room;
    }

    public String getTime() {
        return this.time;
    }

    public String getPercent() {
        return this.percent;
    }

    public String getDamage() {
        return this.damage;
    }

    public String getSplits() {
        return this.splits;
    }

    public String getHealed() {
        return this.healed;
    }

    public SpoonTobStatsConfig getConfig() {
        return this.config;
    }

    SpoonTobStatsInfobox(BufferedImage image, SpoonTobStatsConfig config, SpoonTobStatsPlugin plugin, String room, String time, String percent, String damage, String splits, String healed) {
        super(image, plugin);
        this.config = config;
        this.room = room;
        this.time = time;
        this.percent = percent;
        this.damage = damage;
        this.splits = splits;
        this.healed = healed;
        setPriority(InfoBoxPriority.LOW);
    }

    public String getText() {
        switch (this.config.infoBoxText()) {
            case NONE:
                return "";
            case TIME:
                return StringUtils.substringBefore(this.time, ".");
            case DAMAGE_PERCENT:
                return "" + Math.round(Double.parseDouble(this.percent)) + "%";
        }
        return "";
    }

    public Color getTextColor() {
        return Color.GREEN;
    }

    public String getTooltip() {
        if (!this.config.infoBoxTooltip())
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append(this.room);
        sb.append("</br>");
        if (this.config.infoBoxTooltipSplits() && !StringUtils.isEmpty(this.splits)) {
            sb.append(this.splits);
            if (this.config.infoBoxTooltipDmg() || this.config.infoBoxTooltipHealed())
                sb.append("</br>");
        }
        if (this.config.infoBoxTooltipDmg() && !StringUtils.isEmpty(this.damage) && !this.damage.equals("0")) {
            sb.append(this.damage).append(" (").append(this.percent).append("%)");
            if (this.config.infoBoxTooltipHealed())
                sb.append("</br>");
        }
        if (this.config.infoBoxTooltipHealed() && !StringUtils.isEmpty(this.healed))
            sb.append(this.healed);
        return sb.toString();
    }

    public boolean render() {
        return this.config.showInfoBoxes();
    }
}

