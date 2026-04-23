package net.reldo.taskstracker.routing;

import net.reldo.taskstracker.data.route.RouteItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransitionInjector {

    public static List<RouteItem> inject(TaskCluster from, TaskCluster to) {
        List<RouteItem> transitions = new ArrayList<>();

        // Always add teleport transition between clusters
        RouteItem teleport = new RouteItem();
        teleport.setIcon(1504);
        teleport.setLabel("Teleport");
        teleport.setDescription("Teleport to " + to.areaName);
        transitions.add(teleport);

        // Check for combat tasks in next cluster
        long combatTaskCount = to.tasks.stream()
                .filter(t -> isCombatTask(t.getName()) || isCombatTask(t.getDescription()))
                .count();

        if (combatTaskCount > 0) {
            RouteItem gearCheck = new RouteItem();
            gearCheck.setLabel("Check gear");
            String combatTasks = to.tasks.stream()
                    .filter(t -> isCombatTask(t.getName()) || isCombatTask(t.getDescription()))
                    .map(t -> t.getName())
                    .collect(Collectors.joining(", "));
            gearCheck.setDescription("Prepare for: " + combatTasks);
            transitions.add(gearCheck);
        }

        // Check for bank stop after current cluster
        long skillingTaskCount = from.tasks.stream()
                .filter(t -> isSkillingTask(t.getName()) || isSkillingTask(t.getDescription()))
                .count();

        if (skillingTaskCount >= 3) {
            RouteItem bankStop = new RouteItem();
            bankStop.setIcon(1453);
            bankStop.setLabel("Bank");
            bankStop.setDescription("Bank before leaving " + from.areaName);
            transitions.add(0, bankStop);
        }

        return transitions;
    }

    private static boolean isCombatTask(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("kill") || lower.contains("defeat") || lower.contains("slay")
                || lower.contains("damage") || lower.contains("fight");
    }

    private static boolean isSkillingTask(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("catch") || lower.contains("chop") || lower.contains("mine")
                || lower.contains("fish") || lower.contains("smith") || lower.contains("craft")
                || lower.contains("fletch") || lower.contains("cook");
    }
}