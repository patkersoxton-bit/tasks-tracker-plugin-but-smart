package net.reldo.taskstracker.routing;

public class ClusterScorer {

    private static final int LLM_BONUS = 15;

    public static double score(TaskCluster cluster, RouteOptimizationConfig config) {
        double score = (cluster.size() * config.sizeWeight())
                + (cluster.totalPoints() / (double) config.pointWeight())
                - (cluster.avgTierWeight() * config.tierPenalty());

        if (cluster.getSource() == TaskCluster.ClusterSource.LLM_IMPORT) {
            score += LLM_BONUS;
        }

        return score;
    }
}
