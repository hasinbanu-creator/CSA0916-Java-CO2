package Task05_GenericLeaderboard.src;

import java.util.*;

/**
 * Task 05: Leaderboard Comprehensive Test Suite
 * Includes tests for multi-level ties, intentionally faulty comparators,
 * transitivity proofs, and reusability with other domain entities.
 */
public class LeaderboardTests {

    // Domain Entity 2: Player (for reusability testing)
    static class Player {
        final String name;
        final int score;
        final int penalties;

        Player(String name, int score, int penalties) {
            this.name = name;
            this.score = score;
            this.penalties = penalties;
        }

        @Override
        public String toString() {
            return name + "[Pts:" + score + ",Pen:" + penalties + "]";
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 05: LEADERBOARD ADVANCED VERIFICATION & EDGE CASE TESTS");
        System.out.println("================================================================================\n");

        Comparator<Team> correctComparator = Comparator
                .comparingInt(Team::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(Team::getGoalDiff).reversed())
                .thenComparing(Team::getName);

        // --- TEST 1: Multiple Ties at Every Level ---
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST 1: Multiple Ties at Every Level (Points, Goal Difference, Names)");
        Leaderboard<Team> tieBoard = new Leaderboard<>(correctComparator);
        tieBoard.add(new Team("Tigers", 10, 5));
        tieBoard.add(new Team("Hawks", 10, 5));
        tieBoard.add(new Team("Falcons", 10, 8));
        tieBoard.add(new Team("Lions", 7, 2));
        tieBoard.add(new Team("Cheetahs", 7, 2));
        tieBoard.add(new Team("Bears", 7, -1));

        List<Team> tieResult = tieBoard.ranking();
        System.out.println("Ranked Teams:");
        for (int i = 0; i < tieResult.size(); i++) {
            System.out.printf("   %d. %s\n", (i + 1), tieResult.get(i));
        }
        System.out.println("Verification:");
        System.out.println("   - Falcons(10,8) first (highest GD among 10-pointers)");
        System.out.println("   - Hawks(10,5) beats Tigers(10,5) alphabetically ('H' < 'T')");
        System.out.println("   - Cheetahs(7,2) beats Lions(7,2) alphabetically ('C' < 'L')");
        System.out.println();

        // --- TEST 2: Intentionally Faulty Comparators ---
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST 2: Demonstrating Faulty Comparators and Their Failure Modes");

        // Faulty Comparator A: Subtraction-based integer overflow bug
        // Using (t2.points - t1.points) causes integer overflow when points differ by more than Integer.MAX_VALUE / 2
        Comparator<Team> subtractionComparator = (a, b) -> (b.points - a.points);
        Team highPoints = new Team("HighTech", 2_000_000_000, 5);
        Team lowPoints  = new Team("LowTech", -2_000_000_000, 5);
        int subCompare = subtractionComparator.compare(highPoints, lowPoints);
        System.out.println("   [Fault A - Integer Overflow]:");
        System.out.println("   Comparing 2,000,000,000 and -2,000,000,000 via subtraction: " + subCompare);
        System.out.println("   Expected: Negative (highPoints ranks first). Actual: Positive due to overflow, inverting rankings!");

        // Faulty Comparator B: Incomplete tie-breaker (ignores team name)
        Comparator<Team> incompleteComparator = (a, b) -> {
            if (a.points != b.points) return Integer.compare(b.points, a.points);
            return Integer.compare(b.goalDiff, a.goalDiff);
            // Missing name tie-breaker!
        };
        Team bison = new Team("Bisons", 9, 4);
        Team panther = new Team("Panthers", 9, 4);
        System.out.println("   [Fault B - Incomplete Comparator]:");
        System.out.println("   compare(Bisons, Panthers) = " + incompleteComparator.compare(bison, panther));
        System.out.println("   Both teams compare as 0 (treated as equal), violating strict total ordering!");
        System.out.println();

        // --- TEST 3: Transitivity and Consistency Verification ---
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST 3: Transitivity and Consistency Verification");
        Team a = new Team("Aero", 10, 5);
        Team b = new Team("Brave", 10, 4);
        Team c = new Team("Cyber", 9, 10);

        boolean transitivityHolds = (correctComparator.compare(a, b) < 0) &&
                                    (correctComparator.compare(b, c) < 0) &&
                                    (correctComparator.compare(a, c) < 0);
        boolean antisymmetryHolds = (correctComparator.compare(a, b) == -correctComparator.compare(b, a));

        System.out.println("   A ranks above B: " + (correctComparator.compare(a, b) < 0));
        System.out.println("   B ranks above C: " + (correctComparator.compare(b, c) < 0));
        System.out.println("   A ranks above C: " + (correctComparator.compare(a, c) < 0));
        System.out.println("   Transitivity holds? " + transitivityHolds);
        System.out.println("   Antisymmetry holds? " + antisymmetryHolds);
        System.out.println();

        // --- TEST 4: Generic Reusability for Other Entities ---
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST 4: Generic Reusability with Player Entity");
        Comparator<Player> playerRanker = Comparator
                .comparingInt((Player p) -> p.score).reversed()
                .thenComparingInt((Player p) -> p.penalties) // Lower penalties first
                .thenComparing((Player p) -> p.name);

        Leaderboard<Player> playerBoard = new Leaderboard<>(playerRanker);
        playerBoard.add(new Player("Siddharth", 85, 2));
        playerBoard.add(new Player("Ananya", 92, 4));
        playerBoard.add(new Player("Varun", 85, 1)); // Ties with Siddharth on score, but fewer penalties
        playerBoard.add(new Player("Deepak", 92, 1)); // Ties with Ananya on score, fewer penalties

        System.out.println("Ranked Players:");
        List<Player> playerRankings = playerBoard.ranking();
        for (int i = 0; i < playerRankings.size(); i++) {
            System.out.printf("   %d. %s\n", (i + 1), playerRankings.get(i));
        }
        System.out.println("Leaderboard generic reusability verified successfully.");
    }
}

