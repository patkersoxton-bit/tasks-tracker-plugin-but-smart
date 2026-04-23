package net.reldo.taskstracker.routing;

import net.reldo.taskstracker.data.route.Route;
import net.reldo.taskstracker.data.route.RouteSection;
import net.reldo.taskstracker.data.route.RouteItem;
import net.reldo.taskstracker.data.task.ITask;
import net.reldo.taskstracker.data.task.ITaskType;
import net.reldo.taskstracker.data.task.TaskService;
import net.runelite.api.Client;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RouteGenerator {

    private final Client client;
    private final TaskService taskService;
    private final RouteOptimizationConfig config;

    public RouteGenerator(Client client, TaskService taskService, RouteOptimizationConfig config) {
        this.client = client;
        this.taskService = taskService;
        this.config = config;
    }

    public Route generate(ITaskType taskType) {
        // 1. Get all tasks of this type
        List<ITask> allTasks = taskService.getTasks();

        // 2. Filter to actionable only
        List<ITask> eligible = allTasks.stream()
                .filter(t -> !t.isCompleted())
                .filter(t -> !config.filterEligibleOnly() || meetsSkillRequirements(t))
                .filter(t -> !config.filterUnlockedRegions() || isRegionUnlocked(t))
                .collect(Collectors.toList());

        // 3. Cluster by area
        List<TaskCluster> clusters = buildClusters(eligible);

        // 4. Score and sort clusters (descending score)
        clusters.sort(Comparator.comparingDouble(c -> -ClusterScorer.score(c, config)));

        // 5. Build sections with injected transitions
        List<RouteSection> sections = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            TaskCluster cluster = clusters.get(i);
            sections.add(buildSection(cluster));

            if (i < clusters.size() - 1) {
                TaskCluster next = clusters.get(i + 1);
                List<RouteItem> transitions = TransitionInjector.inject(cluster, next);
                if (!transitions.isEmpty()) {
                    sections.add(new RouteSection("→ " + next.areaName, transitions));
                }
            }
        }

        // 6. Build and return Route
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return new Route(
                UUID.randomUUID().toString(),
                "Auto Route — " + timestamp,
                taskType.getTaskJsonName(),
                "SmartRouter",
                "Generated from " + eligible.size() + " eligible tasks across " + clusters.size() + " areas.",
                sections
        );
    }

    private List<TaskCluster> buildClusters(List<ITask> tasks) {
        Map<String, List<ITask>> byArea = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getStringParam("area", "General")));

        return byArea.entrySet().stream()
                .map(e -> {
                    List<ITask> clusterTasks = e.getValue().stream()
                            .sorted(Comparator.comparingInt((ITask t) -> getTierWeight(t))
                                    .thenComparingInt(t -> -t.getIntParam("points", 0)))
                            .collect(Collectors.toList());
                    return new TaskCluster(e.getKey(), LocationTable.getPoint(e.getKey()), clusterTasks);
                })
                .collect(Collectors.toList());
    }

    private RouteSection buildSection(TaskCluster cluster) {
        List<RouteItem> items = cluster.tasks.stream()
                .map(this::taskToRouteItem)
                .collect(Collectors.toList());
        return new RouteSection(cluster.areaName, items);
    }

    private RouteItem taskToRouteItem(ITask task) {
        RouteItem item = new RouteItem();
        item.setTaskId(task.getIntParam("id"));
        item.setLabel(task.getName());
        item.setDescription(task.getDescription());
        item.setLocation(LocationTable.getPoint(task.getStringParam("area", "General")));
        return item;
    }

    private int getTierWeight(ITask task) {
        String tier = task.getStringParam("tier", "easy");
        switch (tier.toLowerCase()) {
            case "medium": return 1;
            case "hard": return 2;
            case "elite": return 3;
            case "master": return 4;
            default: return 0; // easy
        }
    }

    private boolean meetsSkillRequirements(ITask t) {
        // TODO: Implement skill requirement checking from task data
        // For now return true until task schema is verified
        return true;
    }

    private boolean isRegionUnlocked(ITask t) {
        // TODO: Implement region unlock checking via varbits
        // For now return true until varbits are identified
        return true;
    }
}