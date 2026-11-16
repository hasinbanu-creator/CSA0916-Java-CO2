package Task01_StringPerformance.src;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Task 01: Railway e-Ticket Generation Benchmark
 * Compares String (+ operator in loop), StringBuilder, and StringBuffer
 * under three workloads: Small, Medium, Large.
 */
public class RailwayTicketBenchmark {

    // Helper to generate dummy ticket fields
    public static List<String> generateFields(int count) {
        List<String> fields = new ArrayList<>(count);
        String[] templates = {
            "PNR:2847192048", "Train:12626/KeralaExpress", "From:NDLS", "To:TVC",
            "DOJ:2026-11-20", "Class:3A", "Coach:B4", "Berth:45(MB)", "Quota:GN",
            "Passenger:ArunKumar", "Age:28", "Gender:M", "Status:CNF", "BaseFare:1850",
            "ResvFee:40", "Superfast:45", "GST:96.75", "Total:2031.75", "Gate:3", "BookedAt:IRCTC-App"
        };
        for (int i = 0; i < count; i++) {
            fields.add(templates[i % templates.length] + "_Field" + (i + 1));
        }
        return fields;
    }

    // 1. String concatenation using '+' operator in loop
    public static String buildTicketStringConcat(List<String> fields) {
        String ticket = "";
        for (String field : fields) {
            ticket = ticket + field + "\n";
        }
        return ticket;
    }

    // 2. StringBuilder (unsynchronized, thread-confined)
    public static String buildTicketStringBuilder(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(field).append("\n");
        }
        return sb.toString();
    }

    // 3. StringBuffer (synchronized, thread-safe)
    public static String buildTicketStringBuffer(List<String> fields) {
        StringBuffer sb = new StringBuffer();
        for (String field : fields) {
            sb.append(field).append("\n");
        }
        return sb.toString();
    }

    // Helper to estimate intermediate String objects created
    // For String '+': (fieldsCount * 2) intermediate strings per ticket
    // (field + "\n" creates 1 string, ticket + that creates 1 string)
    public static long estimateIntermediateStrings(String method, int ticketCount, int fieldsCount) {
        if (method.equals("String (+)")) {
            return (long) ticketCount * fieldsCount * 2L;
        } else {
            return 0L; // StringBuilder/StringBuffer reuse internal buffer; 0 intermediate String objects before toString()
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 01: RAILWAY E-TICKET GENERATION PERFORMANCE BENCHMARK");
        System.out.println("  Comparing String (+), StringBuilder, and StringBuffer");
        System.out.println("================================================================================\n");

        int[] ticketCounts = { 1, 1000, 10000 };
        int[] fieldCounts  = { 5,   20,    50 };
        String[] workloadNames = { "Small (1 ticket, 5 fields)", "Medium (1,000 tickets, 20 fields)", "Large (10,000 tickets, 50 fields)" };

        int warmups = 5;
        int trials = 10;

        List<String[]> csvRows = new ArrayList<>();
        csvRows.add(new String[]{ "Workload", "TicketCount", "FieldsPerTicket", "Method", "AvgTimeMs", "AvgTimeNs", "EstIntermediateStrings", "ApproxHeapDeltaKB" });

        for (int w = 0; w < ticketCounts.length; w++) {
            int numTickets = ticketCounts[w];
            int numFields = fieldCounts[w];
            String workloadName = workloadNames[w];

            System.out.println(">>> Workload: " + workloadName);
            List<String> fields = generateFields(numFields);

            String[] methods = { "String (+)", "StringBuilder", "StringBuffer" };

            for (String method : methods) {
                // Warm-up phase
                for (int i = 0; i < warmups; i++) {
                    for (int t = 0; t < Math.min(numTickets, 100); t++) {
                        if (method.equals("String (+)")) buildTicketStringConcat(fields);
                        else if (method.equals("StringBuilder")) buildTicketStringBuilder(fields);
                        else buildTicketStringBuffer(fields);
                    }
                }

                // Measurement phase
                long totalTimeNs = 0;
                long totalMemoryDelta = 0;

                for (int r = 0; r < trials; r++) {
                    System.gc();
                    try { Thread.sleep(20); } catch (InterruptedException ignored) {}

                    Runtime rt = Runtime.getRuntime();
                    long memBefore = rt.totalMemory() - rt.freeMemory();

                    long start = System.nanoTime();
                    for (int t = 0; t < numTickets; t++) {
                        if (method.equals("String (+)")) buildTicketStringConcat(fields);
                        else if (method.equals("StringBuilder")) buildTicketStringBuilder(fields);
                        else buildTicketStringBuffer(fields);
                    }
                    long end = System.nanoTime();

                    long memAfter = rt.totalMemory() - rt.freeMemory();
                    totalTimeNs += (end - start);
                    totalMemoryDelta += Math.max(0, memAfter - memBefore);
                }

                double avgTimeNs = (double) totalTimeNs / trials;
                double avgTimeMs = avgTimeNs / 1_000_000.0;
                double avgMemKB = (double) totalMemoryDelta / trials / 1024.0;
                long estIntermediateObjects = estimateIntermediateStrings(method, numTickets, numFields);

                System.out.printf("   %-15s | Avg Time: %10.3f ms (%12.0f ns) | Est. Intermediates: %,10d | Heap Delta: %8.1f KB\n",
                        method, avgTimeMs, avgTimeNs, estIntermediateObjects, avgMemKB);

                csvRows.add(new String[]{
                        workloadName,
                        String.valueOf(numTickets),
                        String.valueOf(numFields),
                        method,
                        String.format("%.4f", avgTimeMs),
                        String.format("%.0f", avgTimeNs),
                        String.valueOf(estIntermediateObjects),
                        String.format("%.2f", avgMemKB)
                });
            }
            System.out.println();
        }

        // Export CSV results
        java.io.File dirResults = new java.io.File("Task01_StringPerformance/results");
        if (!dirResults.exists()) {
            dirResults = new java.io.File("CSA0916-192321030-Assignment2/Task01_StringPerformance/results");
        }
        java.io.File dirBench = new java.io.File("Benchmark_Results");
        if (!dirBench.exists()) {
            dirBench = new java.io.File("CSA0916-192321030-Assignment2/Benchmark_Results");
        }

        java.io.File csv1 = new java.io.File(dirResults, "Q1_String_Benchmark.csv");
        java.io.File csv2 = new java.io.File(dirBench, "Q1_String_Benchmark.csv");
        java.io.File[] targets = { csv1, csv2 };

        for (java.io.File target : targets) {
            target.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                for (String[] row : csvRows) {
                    pw.println(String.join(",", row));
                }
                System.out.println("Benchmark data successfully exported to: " + target.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving CSV to " + target + ": " + e.getMessage());
            }
        }

        System.out.println("\nBenchmark complete.");
    }
}
