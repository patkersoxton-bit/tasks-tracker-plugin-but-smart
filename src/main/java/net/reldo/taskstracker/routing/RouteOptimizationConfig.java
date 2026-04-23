package net.reldo.taskstracker.routing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("smartrouter")
public interface RouteOptimizationConfig extends Config {

    @ConfigItem(keyName = "sizeWeight", name = "Cluster size weight",
        description = "Prioritize areas with more tasks (0–20)")
    @Range(min = 0, max = 20)
    default int sizeWeight() { return 10; }

    @ConfigItem(keyName = "pointWeight", name = "Point value weight",
        description = "Prioritize high-point areas (0–20)")
    @Range(min = 0, max = 20)
    default int pointWeight() { return 5; }

    @ConfigItem(keyName = "tierPenalty", name = "Hard task penalty",
        description = "Deprioritize areas with only hard/elite/master tasks (0–20)")
    @Range(min = 0, max = 20)
    default int tierPenalty() { return 5; }

    @ConfigItem(keyName = "filterEligibleOnly", name = "Eligible tasks only",
        description = "Only include tasks you meet skill requirements for")
    default boolean filterEligibleOnly() { return true; }

    @ConfigItem(keyName = "filterUnlockedRegions", name = "Unlocked regions only",
        description = "Only include tasks in regions you have unlocked")
    default boolean filterUnlockedRegions() { return true; }

    @ConfigItem(keyName = "showDoNextOverlay", name = "Show 'Do Next' overlay",
        description = "Show current route task as a persistent infobox")
    default boolean showDoNextOverlay() { return false; }
}