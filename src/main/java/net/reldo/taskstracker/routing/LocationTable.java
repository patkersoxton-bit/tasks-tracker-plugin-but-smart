package net.reldo.taskstracker.routing;

import net.runelite.api.coords.WorldPoint;
import java.util.Map;

public class LocationTable {
    private static final Map<String, WorldPoint> AREA_POINTS = Map.ofEntries(
        Map.entry("Varlamore", new WorldPoint(1712, 3133, 0)),
        Map.entry("Kebos Lowlands", new WorldPoint(1455, 3631, 0)),
        Map.entry("Kandarin", new WorldPoint(2660, 3305, 0)),
        Map.entry("Asgarnia", new WorldPoint(3007, 3376, 0)),
        Map.entry("Misthalin", new WorldPoint(3213, 3424, 0)),
        Map.entry("Morytania", new WorldPoint(3490, 3265, 0)),
        Map.entry("Karamja", new WorldPoint(2923, 3148, 0)),
        Map.entry("Fremennik", new WorldPoint(2659, 3670, 0)),
        Map.entry("Desert", new WorldPoint(3275, 3163, 0)),
        Map.entry("Tirannwn", new WorldPoint(2341, 3167, 0)),
        Map.entry("Wilderness", new WorldPoint(3098, 3523, 0)),
        Map.entry("General", new WorldPoint(3213, 3424, 0))
    );

    public static WorldPoint getPoint(String area) {
        if (area == null) {
            return AREA_POINTS.get("General");
        }
        return AREA_POINTS.getOrDefault(area, AREA_POINTS.get("General"));
    }
}