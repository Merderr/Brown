package net.runelite.client.plugins.a_Qol_shit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;

import javax.annotation.Nullable;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Slf4j
@PluginDescriptor(
        name = "ToB QoL Improvements",
        description = "Minor QoL improvements to the Theatre of Blood",
        tags = {"tob", "theatre", "theater", "raids2"},
        enabledByDefault = false
)
public class ToBQoLImprovementsPlugin extends Plugin
{
    private static final Set<String> TOB_CHEST_TARGETS = ImmutableSet.of(
            "stamina potion(4)",
            "prayer potion(4)",
            "saradomin brew(4)",
            "super restore(4)",
            "mushroom potato",
            "shark",
            "sea turtle",
            "manta ray"
    );

    @Inject
    private Client client;

    @Inject
    private ToBQoLImprovementsConfig config;

    @Inject
    private ConfigManager configManager;

    // Multimap for swapping value with buy-1
    private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

    @Getter
    private GameObject lootChest;

    @Getter
    boolean chestHasLoot = false;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private RemindersOverlay overlay;

    @Provides
    ToBQoLImprovementsConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ToBQoLImprovementsConfig.class);
    }

    @Override
    public void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    public void shutDown()
    {
        lootChest = null;
        chestHasLoot = false;
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onClientTick(ClientTick clientTick)
    {
        // The menu is not rebuilt when it is open, so don't swap or else it will
        // repeatedly swap entries
        if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
        {
            return;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();

        // Build option map for quick lookup in findIndex
        int index = 0;
        optionIndexes.clear();
        for (MenuEntry entry : menuEntries)
        {
            String option = Text.removeTags(entry.getOption()).toLowerCase();
            optionIndexes.put(option, index++);
        }

        // Perform swaps
        index = 0;
        for (MenuEntry entry : menuEntries)
        {
            swapMenuEntry(index++, entry);
        }

        // Remove the force right-click menu
        if (config.lootChestBankAll() && client.getGameState() == GameState.LOGGED_IN && !client.isMenuOpen() && isInVerSinhaza())
        {
            for (MenuEntry entry : menuEntries)
            {
                if (entry.getOption().equals("Bank-all"))
                {
                    entry.setForceLeftClick(true);
                    break;
                }
            }
            client.setMenuEntries(menuEntries);
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        if (isInVerSinhaza())
        {
            // Determine if chest has loot and draw an arrow overhead
            if (lootChest != null && Objects.requireNonNull(getObjectComposition(lootChest.getId())).getId() == 41435 && !chestHasLoot)
            {
                chestHasLoot = true;
                client.setHintArrow(lootChest.getWorldLocation());
            }

            // Clear the arrow if the loot is taken
            if (lootChest != null && Objects.requireNonNull(getObjectComposition(lootChest.getId())).getId() == 41436 && chestHasLoot)
            {
                chestHasLoot = false;
                client.clearHintArrow();
            }
        }
        else
        {
            if (lootChest != null)
            {
                lootChest = null;
            }
        }
    }

    @Nullable
    private ObjectComposition getObjectComposition(int id)
    {
        ObjectComposition objectComposition = client.getObjectDefinition(id);
        return objectComposition.getImpostorIds() == null ? objectComposition : objectComposition.getImpostor();
    }

    private static final Set<Integer> VER_SINHAZA_REGIONS = ImmutableSet.of(
            14386,
            14642
    );

    public boolean isInVerSinhaza()
    {
        return VER_SINHAZA_REGIONS.contains(Objects.requireNonNull(client.getLocalPlayer()).getWorldLocation().getRegionID());
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        if (event.getGameObject().getId() == 41437)
        {
            lootChest = event.getGameObject();
        }
    }

    private void swapMenuEntry(int index, MenuEntry menuEntry)
    {
        final String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
        final String target = Text.removeTags(menuEntry.getTarget()).toLowerCase();

        // Swap the "Value" option with "Buy-1" for the given target if it's not off
        if (option.equals("value") && !config.tobSupplyChestBuy().toString().equals("value"))
        {
            if (TOB_CHEST_TARGETS.contains(target))
            {
                swap(option, target, index);
            }
        }
    }

    private void swap(String option, String target, int index)
    {
        MenuEntry[] menuEntries = client.getMenuEntries();

        int thisIndex = findIndex(menuEntries, index, option, target);
        int optionIdx = findIndex(menuEntries, thisIndex, config.tobSupplyChestBuy().toString(), target);

        if (thisIndex >= 0 && optionIdx >= 0)
        {
            swap(optionIndexes, menuEntries, optionIdx, thisIndex);
        }
    }

    private int findIndex(MenuEntry[] entries, int limit, String option, String target)
    {
        List<Integer> indexes = optionIndexes.get(option);

        // We want the last index which matches the target, as that is what is top-most on the menu
        for (int i = indexes.size() - 1; i >= 0; i--)
        {
            int index = indexes.get(i);
            MenuEntry entry = entries[index];
            String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

            // Limit to the last index which is prior to the current entry
            if (index <= limit && entryTarget.equals(target))
            {
                return index;
            }
        }

        return -1;
    }

    private void swap(ArrayListMultimap<String, Integer> optionIndexes, MenuEntry[] entries, int index1, int index2)
    {
        MenuEntry entry = entries[index1];
        entries[index1] = entries[index2];
        entries[index2] = entry;

        client.setMenuEntries(entries);

        // Rebuild option indexes
        optionIndexes.clear();
        int idx = 0;
        for (MenuEntry menuEntry : entries)
        {
            String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
            optionIndexes.put(option, idx++);
        }
    }


    private static final int VERZIK_P2_AREA_SOUND_EFFECT_ID = 3987;
    @Subscribe
    public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed areaSoundEffectPlayed) {
        int soundId = areaSoundEffectPlayed.getSoundId();
        if (config.muteVerzikP2AreaSounds() && soundId == VERZIK_P2_AREA_SOUND_EFFECT_ID)
        {
            areaSoundEffectPlayed.consume();
        }
    }
}
