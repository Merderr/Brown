package net.runelite.client.plugins.neverlogout;

import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "<html><font color=#25c550>[S] Never Logout", description = "Enable this and you will never log out")
public class NeverLog extends Plugin {
    @Inject
    private Client client;

    private Random random = new Random();

    private long randomDelay;

    protected void startUp() {
        this.randomDelay = randomDelay();
    }

    protected void shutDown() {}

    @Subscribe
    public void onGameTick(GameTick event) {
        if (checkIdleLogout()) {
            randomDelay = randomDelay();
            Executors.newSingleThreadExecutor()
                    .submit(this::pressKey);
        }
    }

    private boolean checkIdleLogout() {
        int idleClientTicks = client.getKeyboardIdleTicks();
        if (client.getMouseIdleTicks() < idleClientTicks)
            idleClientTicks = client.getMouseIdleTicks();
        return (idleClientTicks >= randomDelay);
    }

    private long randomDelay() {
        return (long)clamp(
                Math.round(random.nextGaussian() * 8000.0D));
    }

    private static double clamp(double val) {
        return Math.max(1.0D, Math.min(13000.0D, val));
    }

    private void pressKey() {
        KeyEvent keyPress = new KeyEvent(client.getCanvas(), 401, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(client.getCanvas(), 402, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyRelease);
        KeyEvent keyTyped = new KeyEvent(client.getCanvas(), 400, System.currentTimeMillis(), 0, 8);
        client.getCanvas().dispatchEvent(keyTyped);
    }
}

