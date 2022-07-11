package net.runelite.client.plugins.hotkeyablemenuswaps;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.client.config.Keybind;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
enum OccultAltarSwap {
    VENERATE("venerate", HotkeyableMenuSwapsConfig::getSwapVenerateHotkey),
    STANDARD("standard", HotkeyableMenuSwapsConfig::getSwapStandardHotkey),
    ANCIENT("ancient", HotkeyableMenuSwapsConfig::getSwapAncientHotkey),
    LUNAR("lunar", HotkeyableMenuSwapsConfig::getSwapLunarHotkey),
    ARCEUUS("arceuus", HotkeyableMenuSwapsConfig::getSwapArceuusHotkey),
    ;

    public static OccultAltarSwap getCurrentSpellbookMenuOption(Client client) {
        return values()[client.getVarbitValue(4070) + 1]; // 4070 indicated the player's current spellbook.
    }

    private final String menuOptionName;
    private final Function<HotkeyableMenuSwapsConfig, Keybind> keybindFunction;

    public Keybind getKeybind(HotkeyableMenuSwapsConfig config) {
        return keybindFunction.apply(config);
    }
}
