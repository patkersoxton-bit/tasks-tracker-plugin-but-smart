package net.reldo.taskstracker.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.reldo.taskstracker.data.task.ITask;
import java.util.*;
import java.util.stream.Collectors;

public class ClusterImportValidator {

    public static ValidationResult validate(String json, List<ITask> allLeagueTasks) {
        ValidationResult result = new ValidationResult();
        Set<Integer> validTaskIds = allLeagueTasks.stream()
                .map(t -> t.getIntParam("id"))
                .collect(Collectors.toSet());

        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            // Required fields
            if (!root.has("schemaVersion") || root.get("schemaVersion").getAsInt() != 1) {
                result.addError("schemaVersion must be exactly 1");
            }

            if (!root.has("taskType") || root.get("taskType").getAsString().trim().isEmpty()) {
                result.addError("taskType is required");
            }

            if (!root.has("playerName") || root.get("playerName").getAsString().trim().isEmpty()) {
                result.addError("playerName is required");
            }

            if (!root.has("clusters") || !root.get("clusters").isJsonArray()) {
                result.addError("clusters array is required");
                return result;
            }

            JsonArray clusters = root.getAsJsonArray("clusters");
            if (clusters.size() == 0) {
                result.addError("clusters array cannot be empty");
            }

            Set<Integer> seenTaskIds = new HashSet<>();
            int validClusters = 0;
            int totalCoveredTasks = 0;

            for (int i = 0; i < clusters.size(); i++) {
                JsonObject cluster = clusters.get(i).getAsJsonObject();
                String clusterId = cluster.get("id").getAsString();

                if (clusterId == null || clusterId.trim().isEmpty() || clusterId.contains(" ")) {
                    result.addError(String.format("Cluster %d: id must be non-empty with no spaces", i));
                    continue;
                }

                if (cluster.has("name") && cluster.get("name").getAsString().length() > 40) {
                    result.addError(String.format("Cluster '%s': name must be <= 40 characters", clusterId));
                }

                if (!cluster.has("taskIds") || !cluster.get("taskIds").isJsonArray()) {
                    result.addError(String.format("Cluster '%s': taskIds array is required", clusterId));
                    continue;
                }

                JsonArray taskIds = cluster.getAsJsonArray("taskIds");
                for (int j = 0; j < taskIds.size(); j++) {
                    int taskId = taskIds.get(j).getAsInt();
                    if (!validTaskIds.contains(taskId)) {
                        result.addError(String.format("Cluster '%s': taskId %d not found in task list", clusterId, taskId));
                        continue;
                    }
                    if (seenTaskIds.contains(taskId)) {
                        result.addError(String.format("taskId %d appears in multiple clusters", taskId));
                    }
                    seenTaskIds.add(taskId);
                    totalCoveredTasks++;
                }

                validClusters++;
            }

            // Check unclustered array
            if (root.has("unclustered") && root.get("unclustered").isJsonArray()) {
                JsonArray unclustered = root.getAsJsonArray("unclustered");
                for (int i = 0; i < unclustered.size(); i++) {
                    int taskId = unclustered.get(i).getAsInt();
                    if (seenTaskIds.contains(taskId)) {
                        result.addError(String.format("taskId %d appears in both clusters and unclustered", taskId));
                    }
                    seenTaskIds.add(taskId);
                }

                if (unclustered.size() > 0) {
                    result.addWarning(String.format("%d tasks left unclustered (will be routed geographically)", unclustered.size()));
                }
            }

            long eligibleCount = allLeagueTasks.stream().filter(t -> !t.isCompleted()).count();
            if (totalCoveredTasks < eligibleCount * 0.5) {
                result.addWarning(String.format("Clusters only cover %d of %d eligible tasks (less than 50%%)",
                        totalCoveredTasks, eligibleCount));
            }

            result.setValid(result.getErrors().isEmpty());
            result.setClusterCount(validClusters);
            result.setTaskCount(totalCoveredTasks);

        } catch (Exception e) {
            result.addError("Invalid JSON: " + e.getMessage());
        }

        return result;
    }

    public static class ValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private int clusterCount;
        private int taskCount;

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public List<String> getErrors() { return errors; }
        public void addError(String error) { errors.add(error); }

        public List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { warnings.add(warning); }

        public int getClusterCount() { return clusterCount; }
        public void setClusterCount(int clusterCount) { this.clusterCount = clusterCount; }

        public int getTaskCount() { return taskCount; }
        public void setTaskCount(int taskCount) { this.taskCount = taskCount; }
    }
}