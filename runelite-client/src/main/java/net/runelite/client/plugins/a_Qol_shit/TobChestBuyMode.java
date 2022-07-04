package net.runelite.client.plugins.a_Qol_shit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TobChestBuyMode
{
    OFF("value"),
    BUY_1("buy-1"),
    BUY_ALL("buy-all"),
    BUY_X("buy-x");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}

