package net.reldo.taskstracker.llm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SystemPromptBuilder {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String build(PlayerContextSnapshot snapshot) {
        StringBuilder prompt = new StringBuilder();

        // Section 1 - Role & Goal
        prompt.append("You are an Old School RuneScape Leagues optimization assistant. Your task is to\n");
        prompt.append("analyze a player's current state in the Demonic Pacts League (League 6) and group\n");
        prompt.append("their available tasks into logical clusters that can be completed efficiently in\n");
        prompt.append("the same session or area. Your output will be imported directly into a RuneLite\n");
        prompt.append("plugin and must exactly match the JSON schema defined at the end of this prompt.\n\n");

        // Section 2 - Player State
        prompt.append("=== PLAYER STATE ===\n");
        prompt.append("Player Name: ").append(snapshot.getPlayerName()).append("\n");
        prompt.append("Task Type: ").append(snapshot.getTaskType()).append("\n");
        prompt.append("Exported: ").append(snapshot.getExportTimestamp()).append("\n");
        prompt.append("Combat Style: ").append(snapshot.getCombatStyle()).append("\n");
        prompt.append("Unlocked Regions: ").append(String.join(", ", snapshot.getUnlockedRegions())).append("\n");
        prompt.append("Selected Relics: ").append(String.join(", ", snapshot.getSelectedRelics())).append("\n\n");

        prompt.append("Skill Levels:\n");
        snapshot.getSkillLevels().forEach((skill, level) ->
                prompt.append("  ").append(String.format("%-12s", skill)).append(" → ").append(level).append("\n"));

        prompt.append("\nCompleted Quests: ").append(String.join(", ", snapshot.getCompletedQuests())).append("\n");
        prompt.append("Completed Tasks: ").append(snapshot.getCompletedTaskIds().size()).append(" tasks\n");
        prompt.append("Eligible Tasks: ").append(snapshot.getEligibleTaskIds().size()).append(" tasks\n");
        prompt.append("Ineligible Tasks: ").append(snapshot.getIneligibleTaskIds().size()).append(" tasks\n\n");

        // Section 3 - Full Task Reference
        prompt.append("=== ALL TASKS ===\n");
        prompt.append(GSON.toJson(snapshot.getAllTasks())).append("\n\n");

        // Section 4 - Clustering Instructions
        prompt.append("=== CLUSTERING INSTRUCTIONS ===\n");
        prompt.append("Group the ELIGIBLE tasks into logical clusters. Each cluster should represent tasks\n");
        prompt.append("a player can accomplish in one focused session. Consider:\n");
        prompt.append("- Geographic proximity and area grouping\n");
        prompt.append("- Teleport availability given the player's relics (home teleport, jewellery, etc.)\n");
        prompt.append("- Combat style compatibility — group tasks needing similar gear/prayer setups\n");
        prompt.append("- Efficient bank trip patterns — flag clusters needing frequent gear swaps\n");
        prompt.append("- Skill gating — do not include tasks whose requirements exceed current levels\n");
        prompt.append("- Relic synergies — weight tasks that benefit from active relics earlier\n\n");

        // Section 5 - Output Schema
        prompt.append("=== OUTPUT SCHEMA ===\n");
        prompt.append("Respond ONLY with a valid JSON object. No preamble, no explanation, no markdown fences.\n\n");
        prompt.append("{\n");
        prompt.append("  \"schemaVersion\": 1,\n");
        prompt.append("  \"taskType\": \"LEAGUE_6\",\n");
        prompt.append("  \"playerName\": \"string\",\n");
        prompt.append("  \"exportTimestamp\": \"ISO-8601 string\",\n");
        prompt.append("  \"generatedBy\": \"string\",\n");
        prompt.append("  \"clusters\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"id\": \"string (unique slug, no spaces)\",\n");
        prompt.append("      \"name\": \"string (max 40 chars)\",\n");
        prompt.append("      \"description\": \"string (1-2 sentences)\",\n");
        prompt.append("      \"recommendedTeleport\": \"string or null\",\n");
        prompt.append("      \"gearNote\": \"string or null\",\n");
        prompt.append("      \"combatFocus\": boolean,\n");
        prompt.append("      \"taskIds\": [integer, ...]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"unclustered\": [integer, ...],\n");
        prompt.append("  \"notes\": \"string or null\"\n");
        prompt.append("}\n\n");

        // Section 6 - Self Validation
        prompt.append("=== SELF VALIDATION ===\n");
        prompt.append("Before finalizing, verify:\n");
        prompt.append("- Every taskId in every cluster appears in the eligible task ID list\n");
        prompt.append("- No taskId appears in more than one cluster or in both clusters and unclustered\n");
        prompt.append("- All cluster ids are unique slugs with no spaces\n");
        prompt.append("- schemaVersion is exactly 1\n");
        prompt.append("- taskType is exactly \"LEAGUE_6\"\n");

        return prompt.toString();
    }
}