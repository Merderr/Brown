package net.runelite.client.plugins.ticktimers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("TickTimers")
public interface TickTimersConfig extends Config {
    @ConfigSection(keyName = "mainConfig", position = 0, name = "Features", description = "")
    public static final String mainConfig = "Features";

    @ConfigSection(keyName = "bosses", position = 1, name = "Bosses", description = "")
    public static final String bosses = "Bosses";

    @ConfigSection(keyName = "text", position = 2, name = "Text", description = "")
    public static final String text = "Text";

    @ConfigItem(position = 0, keyName = "prayerWidgetHelper", name = "Prayer Widget Helper", description = "Shows you which prayer to click and the time until click.", section = "Features")
    default boolean showPrayerWidgetHelper() {
        return false;
    }

    @ConfigItem(position = 1, keyName = "showHitSquares", name = "Show Hit Squares", description = "Shows you where the melee bosses can hit you from.", section = "Features")
    default boolean showHitSquares() {
        return false;
    }

    @ConfigItem(position = 2, keyName = "changeTickColor", name = "Change Tick Color", description = "If this is enabled, it will change the tick color to white<br> at 1 tick remaining, signaling you to swap.", section = "Features")
    default boolean changeTickColor() {
        return false;
    }

    @ConfigItem(position = 3, keyName = "ignoreNonAttacking", name = "Ignore Non-Attacking", description = "Ignore monsters that are not attacking you", section = "Features")
    default boolean ignoreNonAttacking() {
        return false;
    }

    @ConfigItem(position = 4, keyName = "guitarHeroMode", name = "Guitar Hero Mode", description = "Render \"Guitar Hero\" style prayer helper", section = "Features")
    default boolean guitarHeroMode() {
        return false;
    }

    @ConfigItem(position = 0, keyName = "gwd", name = "God Wars Dungeon", description = "Show tick timers for GWD Bosses. This must be enabled before you zone in.", section = "Bosses")
    default boolean gwd() {
        return true;
    }

    @ConfigItem(position = 0, keyName = "fontStyle", name = "Font Style", description = "Plain | Bold | Italics", section = "Text")
    default FontStyle fontStyle() {
        return FontStyle.BOLD;
    }

    @Range(min = 1, max = 40)
    @ConfigItem(position = 1, keyName = "textSize", name = "Text Size", description = "Text Size for Timers.", section = "Text")
    default int textSize() {
        return 32;
    }

    @ConfigItem(position = 2, keyName = "shadows", name = "Shadows", description = "Adds Shadows to text.", section = "Text")
    default boolean shadows() {
        return false;
    }

    public enum FontStyle {
        BOLD("Bold", 1),
        ITALIC("Italic", 2),
        PLAIN("Plain", 0);

        FontStyle(String name, int font) {
            this.name = name;
            this.font = font;
        }

        private String name;

        private int font;

        String getName() {
            return this.name;
        }

        int getFont() {
            return this.font;
        }

        public String toString() {
            return getName();
        }
    }
}

