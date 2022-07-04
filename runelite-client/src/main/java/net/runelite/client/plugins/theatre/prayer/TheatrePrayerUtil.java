package net.runelite.client.plugins.theatre.prayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class TheatrePrayerUtil {
    public static void updateNextPrayerQueue(Queue<TheatreUpcomingAttack> queue) {
        queue.forEach(TheatreUpcomingAttack::decrementTicks);
        queue.removeIf(TheatreUpcomingAttack::shouldRemove);
    }

    public static Map<Integer, TheatreUpcomingAttack> getTickPriorityMap(Queue<TheatreUpcomingAttack> queue) {
        Map<Integer, TheatreUpcomingAttack> map = new HashMap<>();
        queue.forEach(attack -> {
            if (!map.containsKey(Integer.valueOf(attack.getTicksUntil())))
                map.put(Integer.valueOf(attack.getTicksUntil()), attack);
            if (attack.getPriority() < ((TheatreUpcomingAttack)map.get(Integer.valueOf(attack.getTicksUntil()))).getPriority())
                map.put(Integer.valueOf(attack.getTicksUntil()), attack);
        });
        return map;
    }
}
