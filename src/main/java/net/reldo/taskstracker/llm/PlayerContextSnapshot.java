package net.reldo.taskstracker.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerContextSnapshot {
    private String playerName;
    private String taskType;
    private String exportTimestamp;

    private List<Integer> completedTaskIds;
    private List<Integer> eligibleTaskIds;
    private List<Integer> ineligibleTaskIds;

    private Map<String, Integer> skillLevels;
    private List<String> completedQuests;

    private List<String> selectedRelics;
    private String combatStyle;
    private List<String> unlockedRegions;

    private List<TaskSummary> allTasks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TaskSummary {
        private int id;
        private String name;
        private String description;
        private String tier;
        private String area;
        private int points;
        private Map<String, Integer> skillRequirements;
    }
}