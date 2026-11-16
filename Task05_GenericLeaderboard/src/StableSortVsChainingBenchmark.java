package Task05_GenericLeaderboard.src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Task 05: Alternative Concept Analysis
 * Compares a single chained Comparator pass against 3 successive stable sort passes.
 */
public class StableSortVsChainingBenchmark {

    public static List<Team> generateRandomTeams(int count) {
        List<Team> list = new ArrayList<>(count);
        Random rand = new Random(42);
        for (int i = 0; i < count; i++) {
            list.add(new Team("Team_" + (1000 + rand.nextInt(9000)), rand.nextInt(30), rand.nextInt(40) - 20));
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 05: COMPARATOR CHAINING VS. THREE STABLE SORT PASSES");
        System.out.println("================================================================================\n");

        int[] sizes = { 1000, 10000, 50000 };
        int trials = 5;

        for (int size : sizes) {
            System.out.println(">>> Testing with N = " + size + " teams:");
            List<Team> base = generateRandomTeams(size);

            // Approach A: Single Chained Comparator Pass
            Comparator<Team> chained = Comparator
                    .comparingInt(Team::getPoints).reversed()
                    .thenComparing(Comparator.comparingInt(Team::getGoalDiff).reversed())
                    .thenComparing(Team::getName);

            long totalChainedNs = 0;
            for (int t = 0; t < trials; t++) {
                List<Team> copy = new ArrayList<>(base);
                long start = System.nanoTime();
                copy.sort(chained);
                totalChainedNs += (System.nanoTime() - start);
            }
            double avgChainedMs = (totalChainedNs / (double) trials) / 1_000_000.0;

            // Approach B: Three Successive Stable Sorts (least significant key first: Name -> GD -> Points)
            long totalStableNs = 0;
            for (int t = 0; t < trials; t++) {
                List<Team> copy = new ArrayList<>(base);
                long start = System.nanoTime();
                // 1. Sort by name
                copy.sort(Comparator.comparing(Team::getName));
                // 2. Stable sort by GD (reversed)
                copy.sort(Comparator.comparingInt(Team::getGoalDiff).reversed());
                // 3. Stable sort by Points (reversed)
                copy.sort(Comparator.comparingInt(Team::getPoints).reversed());
                totalStableNs += (System.nanoTime() - start);
            }
            double avgStableMs = (totalStableNs / (double) trials) / 1_000_000.0;

            System.out.printf("   Single Chained Comparator (1 sort pass)  : %8.2f ms\n", avgChainedMs);
            System.out.printf("   Three Stable Sorts (3 distinct sort passes): %8.2f ms\n", avgStableMs);
            System.out.printf("   Chained approach speedup: %.2fx faster!\n\n", (avgStableMs / avgChainedMs));
        }

        System.out.println("Complexity Analysis:");
        System.out.println("   - Chained Comparator : 1 x O(N log N) TimSort comparisons.");
        System.out.println("   - Three Stable Sorts : 3 x O(N log N) TimSort passes; performs 3x the array copies and runs.");
    }
}

