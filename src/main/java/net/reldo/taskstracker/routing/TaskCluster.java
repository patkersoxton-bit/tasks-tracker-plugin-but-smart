package net.reldo.taskstracker.routing;

import net.reldo.taskstracker.data.task.ITask;
import net.runelite.api.coords.WorldPoint;
import java.util.List;

public class TaskCluster {
    public final String areaName;
    public final WorldPoint representativePoint;
    public final List<ITask> tasks;
    private ClusterSource source = ClusterSource.GEOGRAPHIC;

    public enum ClusterSource { GEOGRAPHIC, LLM_IMPORT }

    public TaskCluster(String areaName, WorldPoint representativePoint, List<ITask> tasks) {
        this.areaName = areaName;
        this.representativePoint = representativePoint;
        this.tasks = tasks;
    }

    public ClusterSource getSource() { return source; }
    public void setSource(ClusterSource source) { this.source = source; }

    public int totalPoints() {
        return tasks.stream()
                .mapToInt(t -> t.getIntParam("points", 0))
                .sum();
    }

    public double avgTierWeight() {
        if (tasks.isEmpty()) {
            return 0;
        }
        return tasks.stream()
                .mapToInt(t -> {
                    String tier = t.getStringParam("tier", "easy");
                    switch (tier.toLowerCase()) {
                        case "medium": return 1;
                        case "hard": return 2;
                        case "elite": return 3;
                        case "master": return 4;
                        default: return 0; // easy
                    }
                })
                .average()
                .orElse(0);
    }

    public int size() {
        return tasks.size();
    }
}