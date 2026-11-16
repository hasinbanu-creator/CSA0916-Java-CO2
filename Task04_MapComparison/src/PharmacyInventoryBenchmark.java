package Task04_MapComparison.src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Task 04: Hospital Pharmacy Stock Lookup Benchmark
 * Compares HashMap, TreeMap, and LinkedHashMap across 1,000, 10,000, and 100,000 medicines.
 */
public class PharmacyInventoryBenchmark {

    public static class Medicine {
        final String code;
        final String name;
        final String batchNo;
        int quantity;
        final double unitPrice;

        public Medicine(String code, String name, String batchNo, int quantity, double unitPrice) {
            this.code = code;
            this.name = name;
            this.batchNo = batchNo;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        @Override
        public String toString() {
            return String.format("[%s] %-18s (Qty: %3d, Batch: %s, $%.2f)", code, name, quantity, batchNo, unitPrice);
        }
    }

    public static List<Medicine> generateMedicines(int count) {
        List<Medicine> list = new ArrayList<>(count);
        String[] prefixes = { "PARA", "AMOX", "IBUP", "CETC", "AZIT", "METF", "ATOR", "PANT", "OMEP", "LOSA" };
        Random rand = new Random(101);

        for (int i = 0; i < count; i++) {
            // Unsorted codes to reflect real-world insertion order
            String code = String.format("MED-%06d", rand.nextInt(1_000_000));
            String name = prefixes[i % prefixes.length] + "_" + (i + 1);
            String batch = "BAT-" + (202600 + (i % 100));
            int qty = 10 + rand.nextInt(500);
            double price = 5.0 + rand.nextDouble() * 150.0;
            list.add(new Medicine(code, name, batch, qty, price));
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 04: HOSPITAL PHARMACY INVENTORY BENCHMARK");
        System.out.println("  Comparing HashMap, TreeMap, and LinkedHashMap");
        System.out.println("================================================================================\n");

        int[] inventorySizes = { 1000, 10000, 100000 };
        int lookupCount = 100000;
        int trials = 5;

        List<String[]> csvRows = new ArrayList<>();
        csvRows.add(new String[]{ "InventorySize", "Metric", "HashMap_ms", "TreeMap_ms", "LinkedHashMap_ms" });

        for (int size : inventorySizes) {
            System.out.println(">>> Testing Inventory Size: N = " + String.format("%,d", size) + " Medicines");
            List<Medicine> testMeds = generateMedicines(size);

            // Generate lookup keys (mix of existing and non-existing)
            String[] queryKeys = new String[lookupCount];
            Random rand = new Random(42);
            for (int i = 0; i < lookupCount; i++) {
                if (rand.nextBoolean()) {
                    queryKeys[i] = testMeds.get(rand.nextInt(size)).code;
                } else {
                    queryKeys[i] = String.format("MED-%06d", rand.nextInt(1_000_000));
                }
            }

            // 1. Insertion Time
            double hmInsert = benchmarkInsert(new HashMap<>(), testMeds, trials);
            double tmInsert = benchmarkInsert(new TreeMap<>(), testMeds, trials);
            double lhmInsert = benchmarkInsert(new LinkedHashMap<>(), testMeds, trials);
            System.out.printf("   Insertion Time (N items)     | HM: %8.2f ms | TM: %8.2f ms | LHM: %8.2f ms\n",
                    hmInsert, tmInsert, lhmInsert);
            csvRows.add(new String[]{ String.valueOf(size), "Insertion_ms", f(hmInsert), f(tmInsert), f(lhmInsert) });

            // Populate maps for remaining tests
            Map<String, Medicine> hm = new HashMap<>();
            Map<String, Medicine> tm = new TreeMap<>();
            Map<String, Medicine> lhm = new LinkedHashMap<>();
            for (Medicine m : testMeds) {
                hm.put(m.code, m);
                tm.put(m.code, m);
                lhm.put(m.code, m);
            }

            // 2. 100,000 Lookups
            double hmLookup = benchmarkLookups(hm, queryKeys, trials);
            double tmLookup = benchmarkLookups(tm, queryKeys, trials);
            double lhmLookup = benchmarkLookups(lhm, queryKeys, trials);
            System.out.printf("   100,000 Lookups (get)        | HM: %8.2f ms | TM: %8.2f ms | LHM: %8.2f ms\n",
                    hmLookup, tmLookup, lhmLookup);
            csvRows.add(new String[]{ String.valueOf(size), "100k_Lookups_ms", f(hmLookup), f(tmLookup), f(lhmLookup) });

            // 3. Sorted Code Report Time
            double hmSortedReport = benchmarkSortedReport(hm, trials);
            double tmSortedReport = benchmarkNaturalReport(tm, trials);
            double lhmSortedReport = benchmarkSortedReport(lhm, trials);
            System.out.printf("   Sorted Code Report Time      | HM: %8.2f ms | TM: %8.2f ms | LHM: %8.2f ms\n",
                    hmSortedReport, tmSortedReport, lhmSortedReport);
            csvRows.add(new String[]{ String.valueOf(size), "Sorted_Report_ms", f(hmSortedReport), f(tmSortedReport), f(lhmSortedReport) });

            // 4. Insertion Order Report Time
            double hmInsertOrderReport = -1.0; // HashMap cannot preserve insertion order without external metadata
            double tmInsertOrderReport = -1.0; // TreeMap loses insertion order completely
            double lhmInsertOrderReport = benchmarkNaturalReport(lhm, trials);
            System.out.printf("   Insertion Order Report Time  | HM: %8s    | TM: %8s    | LHM: %8.2f ms\n",
                    "N/A", "N/A", lhmInsertOrderReport);
            csvRows.add(new String[]{ String.valueOf(size), "Insertion_Order_Report_ms", "N/A", "N/A", f(lhmInsertOrderReport) });

            System.out.println();
        }

        // Verify Output Ordering on a small sample
        verifyOrderingDemo();

        exportCSV(csvRows);
        System.out.println("Pharmacy inventory benchmark completed.");
    }

    private static String f(double v) {
        return String.format("%.2f", v);
    }

    private static double benchmarkInsert(Map<String, Medicine> map, List<Medicine> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            map.clear();
            long start = System.nanoTime();
            for (Medicine m : items) map.put(m.code, m);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkLookups(Map<String, Medicine> map, String[] keys, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            int hits = 0;
            for (String k : keys) {
                if (map.get(k) != null) hits++;
            }
            totalNs += (System.nanoTime() - start);
            if (hits < 0) System.out.print("");
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkNaturalReport(Map<String, Medicine> map, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            int count = 0;
            for (Map.Entry<String, Medicine> entry : map.entrySet()) {
                count += entry.getValue().quantity;
            }
            totalNs += (System.nanoTime() - start);
            if (count < 0) System.out.print("");
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkSortedReport(Map<String, Medicine> map, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            // HashMap or LinkedHashMap must extract and sort keys
            List<String> sortedKeys = new ArrayList<>(map.keySet());
            Collections.sort(sortedKeys);
            int count = 0;
            for (String k : sortedKeys) {
                count += map.get(k).quantity;
            }
            totalNs += (System.nanoTime() - start);
            if (count < 0) System.out.print("");
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static void verifyOrderingDemo() {
        System.out.println("--- VERIFYING OUTPUT ORDERING ACROSS MAP IMPLEMENTATIONS ---");
        String[] sampleCodes = { "MED-9002", "MED-1005", "MED-4500", "MED-2200" };

        Map<String, String> hm = new HashMap<>();
        Map<String, String> tm = new TreeMap<>();
        Map<String, String> lhm = new LinkedHashMap<>();

        for (String c : sampleCodes) {
            hm.put(c, "Item_" + c);
            tm.put(c, "Item_" + c);
            lhm.put(c, "Item_" + c);
        }

        System.out.println("Original Insertion Sequence : " + Arrays.toString(sampleCodes));
        System.out.println("HashMap Key Iteration       : " + hm.keySet() + " (Arbitrary bucket hash order)");
        System.out.println("TreeMap Key Iteration       : " + tm.keySet() + " (Naturally sorted code order)");
        System.out.println("LinkedHashMap Key Iteration : " + lhm.keySet() + " (Preserves exact insertion order)");
        System.out.println("------------------------------------------------------------\n");
    }

    private static void exportCSV(List<String[]> rows) {
        File dirResults = new File("Task04_MapComparison/results");
        if (!dirResults.exists()) dirResults = new File("CSA0916-192321030-Assignment2/Task04_MapComparison/results");
        File dirBench = new File("Benchmark_Results");
        if (!dirBench.exists()) dirBench = new File("CSA0916-192321030-Assignment2/Benchmark_Results");

        File[] targets = { new File(dirResults, "Q4_Map_Benchmark.csv"), new File(dirBench, "Q4_Map_Benchmark.csv") };
        for (File target : targets) {
            target.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                for (String[] r : rows) pw.println(String.join(",", r));
                System.out.println("Map benchmark data exported to: " + target.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving CSV to " + target + ": " + e.getMessage());
            }
        }
    }
}

