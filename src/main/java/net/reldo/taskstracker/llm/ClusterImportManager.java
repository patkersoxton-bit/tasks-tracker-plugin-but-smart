package net.reldo.taskstracker.llm;

import com.google.gson.Gson;
import net.reldo.taskstracker.data.task.ITask;
import net.reldo.taskstracker.routing.LocationTable;
import net.reldo.taskstracker.routing.TaskCluster;
import net.runelite.client.config.ConfigManager;
import java.util.*;
import java.util.stream.Collectors;

public class ClusterImportManager {

    private static final Gson GSON = new Gson();
    private static final String CONFIG_KEY_PREFIX = "smartrouter.clusters";

    private final ConfigManager configManager;

    public ClusterImportManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void saveImport(String taskType, String playerName, String validatedJson) {
        String key = String.format("%s.%s.%s", CONFIG_KEY_PREFIX, taskType, playerName);
        configManager.setConfiguration("tasks-tracker", key, validatedJson);
    }

    public String loadImport(String taskType, String playerName) {
        String key = String.format("%s.%s.%s", CONFIG_KEY_PREFIX, taskType, playerName);
        return configManager.getConfiguration("tasks-tracker", key);
    }

    public boolean hasImportedClusters(String taskType, String playerName) {
        return loadImport(taskType, playerName) != null;
    }

    public void clearImport(String taskType, String playerName) {
        String key = String.format("%s.%s.%s", CONFIG_KEY_PREFIX, taskType, playerName);
        configManager.unsetConfiguration("tasks-tracker", key);
    }

    public List<TaskCluster> buildClustersFromImport(String taskType, String playerName, List<ITask> eligibleTasks) {
        String json = loadImport(taskType, playerName);
        if (json == null) {
            return Collections.emptyList();
        }

        Map<Integer, ITask> taskById = eligibleTasks.stream()
                .collect(Collectors.toMap(t -> t.getIntParam("id"), t -> t));

        List<TaskCluster> clusters = new ArrayList<>();

        try {
            LlmImportData importData = GSON.fromJson(json, LlmImportData.class);

            for (LlmImportData.LlmCluster llmCluster : importData.clusters) {
                List<ITask> clusterTasks = llmCluster.taskIds.stream()
                        .map(taskById::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!clusterTasks.isEmpty()) {
                    TaskCluster cluster = new TaskCluster(
                            llmCluster.name,
                            LocationTable.getPoint(clusterTasks.get(0).getStringParam("area", "General")),
                            clusterTasks
                    );
                    cluster.setSource(TaskCluster.ClusterSource.LLM_IMPORT);
                    clusters.add(cluster);
                }
            }

            // Add fallback geographic cluster for remaining eligible tasks
            Set<Integer> coveredTaskIds = clusters.stream()
                    .flatMap(c -> c.getTasks().stream())
                    .map(t -> t.getIntParam("id"))
                    .collect(Collectors.toSet());

            List<ITask> remainingTasks = eligibleTasks.stream()
                    .filter(t -> !coveredTaskIds.contains(t.getIntParam("id")))
                    .collect(Collectors.toList());

            if (!remainingTasks.isEmpty()) {
                TaskCluster fallbackCluster = new TaskCluster(
                        "Other Tasks",
                        LocationTable.getPoint("General"),
                        remainingTasks
                );
                fallbackCluster.setSource(TaskCluster.ClusterSource.GEOGRAPHIC);
                clusters.add(fallbackCluster);
            }

        } catch (Exception e) {
            // Return empty list on invalid data
        }

        return clusters;
    }

    private static class LlmImportData {
        List<LlmCluster> clusters;

        static class LlmCluster {
            String id;
            String name;
            String description;
            String recommendedTeleport;
            String gearNote;
            boolean combatFocus;
            List<Integer> taskIds;
        }
    }
}