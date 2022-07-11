package net.runelite.client.plugins.hotkeyablemenuswaps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
//import static com.hotkeyablemenuswaps.OccultAltarSwap.*;

@ConfigGroup("hotkeyablemenuswaps")
public interface HotkeyableMenuSwapsConfig extends Config
{
    //@ConfigSection(
      //      name = "Bank",
        //    description = "All options that swap entries in the bank",
          //  position = 0
            @ConfigSection(name = "Custom Swaps by merderr", description = "Text-based custom swaps", position = -10)
    String customSwapsSection = "Custom Swaps";

    @ConfigItem(
            keyName = "customSwaps",
            name = "Custom swaps",
            description = "Options to swap to the top.",
            section = customSwapsSection,
            position = 0
    )
    default String customSwaps() {
        return "";
    }

    @ConfigSection(name = "Custom Swaps (shift)", description = "Text-based custom swaps", position = -10)
    String customShiftSwapsSection = "Custom Swaps (shift)";

    @ConfigItem(
            keyName = "customShiftSwaps",
            name = "Custom swaps (shift)",
            description = "Options to swap to the top when shift is held.",
            section = customShiftSwapsSection,
            position = 0
    )
    default String customShiftSwaps() {
        return "";
    }

    @ConfigSection(name = "Custom Hides", description = "Text-based custom menu entry hides", position = -9)
    String customHidesSection = "Custom Hides";

    @ConfigItem(
            keyName = "customHides",
            name = "Custom hides",
            description = "Options to swap to the top.",
            section = customHidesSection,
            position = 1
    )
    default String customHides() {
        return "";
    }

    @ConfigSection(name = "Custom swap instructions", description = "instructions", position = -7)
    String customSwapInstructions = "Custom swap instructions";

    @ConfigItem(
            keyName = "customSwapperInstructions",
            name = "Instructions",
            description = "Options to swap to the top.",
            section = customSwapInstructions,
            position = 2
    )
    default String customSwapperInstructions() {
        return "### Basic use:\nAdd swaps, one per line. Format is \"option,target\". Supports wildcards (\"*\"). You can also swap a menu entry only when the top entry matches some text, by using the format \"option,target,topOption,topTarget\".\nExamples:\npick-lock,trapdoor\nclear,*bush\nuse,cannonball -> dwarf*\ntake,small fishing net,set-trap,young tree\n\n### Hiding:\nYou can hide menu entries with the same syntax, described above.\n\n### Limitations:\nSome menu entries won't be swapped per jagex's rules (player menu entries, spell casts, blackjacking and construction). This includes swapping that entry up, as well as swapping anything while one of these entries is already the left-click entry, as well as hiding entries above construction or blackjack entries.\n";
    }

    @ConfigSection(name = "Bank", description = "All options that swap entries in the bank", position = 0)
    String bankSection = "bank";

    @ConfigItem(
            keyName = "bankSwap1Hotkey",
            name = "Bank 1",
            description = "The hotkey which, when held, swaps the bank's withdraw/deposit 1 option",
            section = bankSection,
            position = 0
    )
    default Keybind getBankSwap1Hotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "bankSwap5Hotkey",
            name = "Bank 5",
            description = "The hotkey which, when held, swaps the bank's withdraw/deposit 5 option",
            section = bankSection,
            position = 1
    )
    default Keybind getBankSwap5Hotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "bankSwap10Hotkey",
            name = "Bank 10",
            description = "The hotkey which, when held, swaps the bank's withdraw/deposit 10 option",
            section = bankSection,
            position = 2
    )
    default Keybind getBankSwap10Hotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "bankSwapXHotkey",
            name = "Bank X",
            description = "The hotkey which, when held, swaps the bank's withdraw/deposit X option",
            section = bankSection,
            position = 3
    )
    default Keybind getBankSwapXHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "bankSwapSetXHotkey",
            name = "Bank Set-X",
            description = "The hotkey which, when held, swaps the bank's withdraw/deposit SetX option",
            section = bankSection,
            position = 4
    )
    default Keybind getBankSwapSetXHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "bankSwapAllHotkey",
            name = "Bank All",
            description = "The hotkey which, when held, swaps the bank's withdraw/deposit All option",
            section = bankSection,
            position = 5
    )
    default Keybind getBankSwapAllHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "bankSwapAllBut1Hotkey",
            name = "Withdraw All-But-1",
            description = "The hotkey which, when held, swaps the bank's withdraw/deposit AllBut1 option",
            section = bankSection,
            position = 6
    )
    default Keybind getBankSwapAllBut1Hotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "bankSwapExtraOpHotkey",
            name = "Eat/Wield/Etc.",
            description = "The hotkey which, when held, swaps the bank's Eat/Wield/etc. option",
            section = bankSection,
            position = 7
    )
    default Keybind getBankSwapExtraOpHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigSection(
            name = "Spirit tree/Fairy ring",
            description = "All options that swap entries on a spirit tree or fairy ring",
            position = 1
    )
    String treeRingSection = "treeRing";
    @ConfigItem(
            keyName = "swapZanaris",
            name = "Zanaris",
            description = "The hotkey which swaps \"Zanaris\"",
            section = treeRingSection,
            position = 0
    )
    default Keybind getSwapZanarisHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "swapTree",
            name = "Tree",
            description = "The hotkey which swaps \"Tree\"",
            section = treeRingSection,
            position = 1
    )
    default Keybind getSwapTreeHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "swapConfigure",
            name = "Configure",
            description = "The hotkey which swaps \"Configure\"",
            section = treeRingSection,
            position = 2
    )
    default Keybind getSwapConfigureHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "swapLastDestination",
            name = "Last-destination",
            description = "The hotkey which swaps \"Last-destination\"",
            section = treeRingSection,
            position = 3
    )
    default Keybind getSwapLastDestinationHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigSection(
            name = "Occult Altar",
            description = "Occult altar menu entry swaps",
            position = 2
    )
    String occultAltarSection = "occultAltar";
    /*@Getter
    @RequiredArgsConstructor
    enum OccultAltarLeftClick {
        OFF(VENERATE, VENERATE),
        STANDARD_VENERATE(STANDARD, VENERATE),
        STANDARD_ANCIENT(STANDARD, ANCIENT),
        STANDARD_LUNAR(STANDARD, LUNAR),
        STANDARD_ARCEUUS(STANDARD, ARCEUUS),
        ANCIENT_VENERATE(ANCIENT, VENERATE),
        ANCIENT_STANDARD(ANCIENT, STANDARD),
        ANCIENT_LUNAR(ANCIENT, LUNAR),
        ANCIENT_ARCEUUS(ANCIENT, ARCEUUS),
        LUNAR_VENERATE(LUNAR, VENERATE),
        LUNAR_STANDARD(LUNAR, STANDARD),
        LUNAR_ANCIENT(LUNAR, ANCIENT),
        LUNAR_ARCEUUS(LUNAR, ARCEUUS),
        ARCEUUS_VENERATE(ARCEUUS, VENERATE),
        ARCEUUS_STANDARD(ARCEUUS, STANDARD),
        ARCEUUS_ANCIENT(ARCEUUS, ANCIENT),
        ARCEUUS_LUNAR(ARCEUUS, LUNAR),
        ;
        private final OccultAltarSwap firstOption;
        private final OccultAltarSwap secondOption;
    }
    @ConfigItem(
            keyName = "occultAltarLeftClick",
            name = "Left click",
            description = "The occult altar's left-click option. The second option is used if you are already on the spellbook of the first option",
            section = occultAltarSection,
            position = 0
    )
    default OccultAltarLeftClick getOccultAltarLeftClickSwap()
    {
        return OccultAltarLeftClick.OFF;
    }*/
    @ConfigItem(
            keyName = "swapVenerateHotkey",
            name = "Venerate",
            description = "The hotkey which swaps \"Venerate\"",
            section = occultAltarSection,
            position = 1
    )
    default Keybind getSwapVenerateHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "swapStandardHotkey",
            name = "Standard",
            description = "The hotkey which swaps \"Standard\"",
            section = occultAltarSection,
            position = 2
    )
    default Keybind getSwapStandardHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "swapAncientHotkey",
            name = "Ancient",
            description = "The hotkey which swaps \"Ancient\"",
            section = occultAltarSection,
            position = 3
    )
    default Keybind getSwapAncientHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "swapLunarHotkey",
            name = "Lunar",
            description = "The hotkey which swaps \"Lunar\"",
            section = occultAltarSection,
            position = 4
    )
    default Keybind getSwapLunarHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "swapArceuusHotkey",
            name = "Arceuus",
            description = "The hotkey which swaps \"Arceuus\"",
            section = occultAltarSection,
            position = 5
    )
    default Keybind getSwapArceuusHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigSection(
            name = "Jewellery box",
            description = "The jewellery box in a player owned house.",
            position = 3
    )
    String jewelleryBoxSection = "jewelleryBox";
    @ConfigItem(
            keyName = "swapJewelleryBoxHotkey",
            name = "Swap Jewellery Box",
            description = "swap the second with the first menu option on the Jewellery Box. Left-click swaps can be changed in the default Menu Entry Swapper.",
            section = jewelleryBoxSection,
            position = 0
    )
    default Keybind getSwapJewelleryBoxHotkey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigSection(
            name = "Portal Nexus",
            description = "Things in the player owned house portal nexus.",
            position = 4
    )
    String portalNexusSection = "portalNexus";
    @ConfigItem(
            keyName = "portalNexusDestinationSwapHotKey",
            name = "Nexus Destination",
            description = "The configured default destination",
            section = portalNexusSection,
            position = 0
    )
    default Keybind portalNexusDestinationSwapHotKey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "portalNexusTeleportMenuSwapHotKey",
            name = "Nexus Teleport Menu",
            description = "",
            section = portalNexusSection,
            position = 1
    )
    default Keybind portalNexusTeleportMenuSwapHotKey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "portalNexusConfigurationSwapHotKey",
            name = "Nexus Configuration",
            description = "The option named \"Configuration\"",
            section = portalNexusSection,
            position = 2
    )
    default Keybind portalNexusConfigurationSwapHotKey()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "pohXericsTalismanLeftClick",
            name = "Xeric's Left Click",
            description = "",
            section = portalNexusSection,
            position = 3
    )
    default HotkeyableMenuSwapsPlugin.PortalNexusXericsTalismanSwap pohXericsTalismanLeftClick()
    {
        return HotkeyableMenuSwapsPlugin.PortalNexusXericsTalismanSwap.DESTINATION;
    }
    @ConfigItem(
            keyName = "pohXericsTalismanDestination",
            name = "Xeric's Destination",
            description = "The configured default destination",
            section = portalNexusSection,
            position = 4
    )
    default Keybind pohXericsTalismanDestination()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "pohXericsTalismanTeleportMenu",
            name = "Xeric's Teleport Menu",
            description = "",
            section = portalNexusSection,
            position = 5
    )
    default Keybind pohXericsTalismanTeleportMenu()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "pohXericsTalismanConfiguration",
            name = "Xeric's Configuration",
            description = "The option named \"Configuration\"",
            section = portalNexusSection,
            position = 6
    )
    default Keybind pohXericsTalismanConfiguration()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "pohDigsitePendantLeftClick",
            name = "Digsite Left Click",
            description = "",
            section = portalNexusSection,
            position = 7
    )
    default HotkeyableMenuSwapsPlugin.PortalNexusDigsitePendantSwap pohDigsitePendantLeftClick()
    {
        return HotkeyableMenuSwapsPlugin.PortalNexusDigsitePendantSwap.DESTINATION;
    }
    @ConfigItem(
            keyName = "pohDigsitePendantDestination",
            name = "Digsite Destination",
            description = "The configured default destination",
            section = portalNexusSection,
            position = 8
    )
    default Keybind pohDigsitePendantDestination()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "pohDigsitePendantTeleportMenu",
            name = "Digsite Teleport Menu",
            description = "",
            section = portalNexusSection,
            position = 9
    )
    default Keybind pohDigsitePendantTeleportMenu()
    {
        return Keybind.NOT_SET;
    }
    @ConfigItem(
            keyName = "pohDigsitePendantConfiguration",
            name = "Digsite Configuration",
            description = "The option named \"Configuration\"",
            section = portalNexusSection,
            position = 10
    )
    default Keybind pohDigsitePendantConfiguration()
    {
        return Keybind.NOT_SET;
    }
    @ConfigSection(
            name = "Inventory",
            description = "Inventory menu entry swaps",
            position = 5
    )
    String inventorySection = "inventory";
    @ConfigItem(
            keyName = "swapUseHotkey",
            name = "Swap Use",
            description = "swap \"Use\" on items in the inventory",
            section = inventorySection,
            position = 0
    )
    default Keybind getSwapUseHotkey()
    {
        return Keybind.NOT_SET;
    }
//	@ConfigSection(
//		name = "Other",
//		description = "Other swaps",
//		position = 4
//	)
//	String otherSection = "other";
//
//	@ConfigItem(
//		keyName = "swapJewelleryBoxHotkey",
//		name = "Swap Jewellery Box",
//		description = "swap the second with the first menu option on the Jewellery Box",
//		section = otherSection,
//		position = 0
//	)
//	default Keybind getSwapJewelleryBoxHotkey()
//	{
//		return Keybind.NOT_SET;
//	}
//
//	@ConfigItem(
//		keyName = "swapPortalNexusHotkey",
//		name = "Swap Portal Nexus",
//		description = "swap the second with the first menu option on the Portal Nexus",
//		section = otherSection,
//		position = 1
//	)
//	default Keybind getSwapPortalNexusHotkey()
//	{
//		return Keybind.NOT_SET;
//	}
//
}