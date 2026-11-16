package Task07_DesignChallenge.src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Task 07: Comprehensive Benchmark Validating the Redesigned Solution
 * Compares Original (Binary Heap PriorityQueue) vs Redesigned (Multi-Bucket FIFO Queue).
 */
public class TriageRedesignBenchmark {

    public static List<OriginalTriageSystem.Patient> generateOrigPatients(int count) {
        List<OriginalTriageSystem.Patient> list = new ArrayList<>(count);
        Random rand = new Random(42);
        for (int i = 0; i < count; i++) {
            // Simulated arrival spaced over time
            long arrivalTime = i * 1000L;
            int sev = 1 + rand.nextInt(5);
            list.add(new OriginalTriageSystem.Patient("P_" + i, sev, arrivalTime));
        }
        return list;
    }

    public static List<RedesignedBucketTriageSystem.Patient> generateRedesignPatients(int count) {
        List<RedesignedBucketTriageSystem.Patient> list = new ArrayList<>(count);
        Random rand = new Random(42);
        for (int i = 0; i < count; i++) {
            long arrivalTime = i * 1000L;
            int sev = 1 + rand.nextInt(5);
            list.add(new RedesignedBucketTriageSystem.Patient("P_" + i, sev, arrivalTime));
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 07: DAY-4 DESIGN CHALLENGE - VALIDATION BENCHMARK");
        System.out.println("  Original (Binary Heap PriorityQueue) vs. Redesigned (Multi-Level FIFO Buckets)");
        System.out.println("================================================================================\n");

        int[] sizes = { 1000, 10000, 100000 };
        int trials = 5;

        List<String[]> csvRows = new ArrayList<>();
        csvRows.add(new String[]{ "QueueSize", "Operation", "Original_ms", "Redesigned_ms", "SpeedupMultiplier", "ImprovementPercent" });

        for (int size : sizes) {
            System.out.println(">>> WORKLOAD: N = " + String.format("%,d", size) + " Patients");
            List<OriginalTriageSystem.Patient> origPatients = generateOrigPatients(size);
            List<RedesignedBucketTriageSystem.Patient> redesignPatients = generateRedesignPatients(size);

            // 1. Admission Benchmark
            double origAdmit = benchmarkOrigAdmit(origPatients, trials);
            double redAdmit = benchmarkRedesignAdmit(redesignPatients, trials);
            printComparison("Admit " + size + " Patients", origAdmit, redAdmit);
            csvRows.add(buildCsvRow(size, "Admit", origAdmit, redAdmit));

            // 2. Polling / Serving Benchmark
            double origPoll = benchmarkOrigPoll(origPatients, trials);
            double redPoll = benchmarkRedesignPoll(redesignPatients, trials);
            printComparison("Poll " + size + " Patients", origPoll, redPoll);
            csvRows.add(buildCsvRow(size, "Poll", origPoll, redPoll));

            // 3. Aging Batch Update Benchmark (at simulation time = size * 500 ms)
            long simTime = (long) size * 500L;
            long agingInterval = 30_000L; // 30 seconds
            double origAging = benchmarkOrigAging(origPatients, simTime, agingInterval, trials);
            double redAging = benchmarkRedesignAging(redesignPatients, simTime, agingInterval, trials);
            printComparison("Aging Update (" + size + " pts)", origAging, redAging);
            csvRows.add(buildCsvRow(size, "Aging_Batch_Update", origAging, redAging));

            System.out.println();
        }

        exportCSV(csvRows);
        System.out.println("Design Challenge benchmark completed successfully.");
    }

    private static String[] buildCsvRow(int size, String op, double orig, double red) {
        double speedup = orig / red;
        double improvement = ((orig - red) / orig) * 100.0;
        return new String[]{
                String.valueOf(size),
                op,
                String.format("%.3f", orig),
                String.format("%.3f", red),
                String.format("%.2fx", speedup),
                String.format("%.1f%%", improvement)
        };
    }

    private static void printComparison(String label, double orig, double red) {
        double speedup = orig / red;
        double improvement = ((orig - red) / orig) * 100.0;
        System.out.printf("   %-28s | Orig: %8.3f ms | Redesign: %8.3f ms | %6.2fx faster (%5.1f%% improvement)\n",
                label, orig, red, speedup, improvement);
    }

    // --- Benchmarking Methods ---

    private static double benchmarkOrigAdmit(List<OriginalTriageSystem.Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            OriginalTriageSystem sys = new OriginalTriageSystem();
            long start = System.nanoTime();
            for (OriginalTriageSystem.Patient p : items) sys.admit(p);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkRedesignAdmit(List<RedesignedBucketTriageSystem.Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            RedesignedBucketTriageSystem sys = new RedesignedBucketTriageSystem();
            long start = System.nanoTime();
            for (RedesignedBucketTriageSystem.Patient p : items) sys.admit(p);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkOrigPoll(List<OriginalTriageSystem.Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            OriginalTriageSystem sys = new OriginalTriageSystem();
            for (OriginalTriageSystem.Patient p : items) sys.admit(p);
            long start = System.nanoTime();
            while (!sys.isEmpty()) sys.poll();
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkRedesignPoll(List<RedesignedBucketTriageSystem.Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            RedesignedBucketTriageSystem sys = new RedesignedBucketTriageSystem();
            for (RedesignedBucketTriageSystem.Patient p : items) sys.admit(p);
            long start = System.nanoTime();
            while (!sys.isEmpty()) sys.poll();
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkOrigAging(List<OriginalTriageSystem.Patient> items, long simTime, long interval, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            OriginalTriageSystem sys = new OriginalTriageSystem();
            for (OriginalTriageSystem.Patient p : items) sys.admit(p);
            long start = System.nanoTime();
            sys.applyAging(simTime, interval);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkRedesignAging(List<RedesignedBucketTriageSystem.Patient> items, long simTime, long interval, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            RedesignedBucketTriageSystem sys = new RedesignedBucketTriageSystem();
            for (RedesignedBucketTriageSystem.Patient p : items) sys.admit(p);
            long start = System.nanoTime();
            sys.applyAging(simTime, interval);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static void exportCSV(List<String[]> rows) {
        File dirResults = new File("Task07_DesignChallenge/results");
        if (!dirResults.exists()) dirResults = new File("CSA0916-192321030-Assignment2/Task07_DesignChallenge/results");
        File dirBench = new File("Benchmark_Results");
        if (!dirBench.exists()) dirBench = new File("CSA0916-192321030-Assignment2/Benchmark_Results");

        File[] targets = { new File(dirResults, "Q7_Redesign_Benchmark.csv"), new File(dirBench, "Q7_Redesign_Benchmark.csv") };
        for (File target : targets) {
            target.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                for (String[] r : rows) pw.println(String.join(",", r));
                System.out.println("Redesign comparison CSV exported to: " + target.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving CSV to " + target + ": " + e.getMessage());
            }
        }
    }
}

