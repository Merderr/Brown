package net.runelite.client.plugins.theatre;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

@Singleton
public abstract class Room {
    protected final TheatrePlugin plugin;

    protected final TheatreConfig config;

    @Inject
    protected OverlayManager overlayManager;

    @Inject
    private Client client;

    @Inject
    protected Room(TheatrePlugin plugin, TheatreConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void init() {}

    public void load() {}

    public void unload() {}

    public boolean inRoomRegion(Integer roomRegionId) {
        return ArrayUtils.contains(this.client.getMapRegions(), roomRegionId.intValue());
    }
}
