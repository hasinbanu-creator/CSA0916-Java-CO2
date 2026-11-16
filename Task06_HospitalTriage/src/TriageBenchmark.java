package Task06_HospitalTriage.src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Task 06: Emergency Triage Benchmark
 * Compares PriorityQueue, Sorted LinkedList, and Five FIFO Queues (Bucket Queue)
 * across queue sizes: 1,000, 10,000, and 100,000 patients.
 */
public class TriageBenchmark {

    // Helper: Five FIFO Queues Alternative (Bucket Queue)
    public static class FiveQueueTriage {
        @SuppressWarnings("unchecked")
        private final Queue<Patient>[] buckets = new ArrayDeque[5];

        public FiveQueueTriage() {
            for (int i = 0; i < 5; i++) {
                buckets[i] = new ArrayDeque<>();
            }
        }

        public void admit(Patient p) {
            buckets[p.severity - 1].add(p); // O(1)
        }

        public Patient poll() {
            for (int i = 0; i < 5; i++) { // O(1) checking at most 5 pointers
                if (!buckets[i].isEmpty()) {
                    return buckets[i].poll();
                }
            }
            return null;
        }

        public boolean isEmpty() {
            for (int i = 0; i < 5; i++) {
                if (!buckets[i].isEmpty()) return false;
            }
            return true;
        }
    }

    // Helper: Sorted LinkedList Alternative
    public static class SortedLinkedListTriage {
        private final LinkedList<Patient> list = new LinkedList<>();

        public void admit(Patient p) {
            // O(N) linear search for insertion position
            ListIterator<Patient> it = list.listIterator();
            while (it.hasNext()) {
                Patient cur = it.next();
                if (p.severity < cur.severity || (p.severity == cur.severity && p.arrival < cur.arrival)) {
                    it.previous();
                    it.add(p);
                    return;
                }
            }
            list.add(p);
        }

        public Patient poll() {
            return list.isEmpty() ? null : list.removeFirst(); // O(1)
        }
    }

    public static List<Patient> generatePatients(int count) {
        List<Patient> list = new ArrayList<>(count);
        Random rand = new Random(101);
        for (int i = 0; i < count; i++) {
            list.add(new Patient("Pat_" + i, 1 + rand.nextInt(5), i));
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 06: EMERGENCY TRIAGE BENCHMARK");
        System.out.println("  Comparing PriorityQueue, Sorted LinkedList, and 5-Queue Bucket Architecture");
        System.out.println("================================================================================\n");

        int[] sizes = { 1000, 10000, 100000 };
        int trials = 5;

        List<String[]> csvRows = new ArrayList<>();
        csvRows.add(new String[]{ "QueueSize", "Operation", "PriorityQueue_ms", "SortedLinkedList_ms", "FiveQueueBuckets_ms" });

        for (int size : sizes) {
            System.out.println(">>> Testing Triage Queue Size: N = " + String.format("%,d", size) + " Patients");
            List<Patient> patients = generatePatients(size);

            // 1. Admission (Enqueue) Time
            double pqAdmit = benchmarkPQAdmit(patients, trials);
            // For Sorted LinkedList, at 100,000 elements O(N^2) would take hours, so we cap or extrapolate
            double sllAdmit = (size <= 10000) ? benchmarkSLLAdmit(patients, trials) : -1.0;
            double fqAdmit = benchmarkFQAdmit(patients, trials);

            System.out.printf("   Admit (N Patients)   | PQ: %8.2f ms | SLL: %8s | 5-Buckets: %8.2f ms\n",
                    pqAdmit, (sllAdmit < 0 ? "O(N^2) Skip" : String.format("%.2f ms", sllAdmit)), fqAdmit);
            csvRows.add(new String[]{ String.valueOf(size), "Admit_N_Patients", f(pqAdmit), (sllAdmit < 0 ? "N/A" : f(sllAdmit)), f(fqAdmit) });

            // 2. Serving (Dequeue) Time
            double pqPoll = benchmarkPQPoll(patients, trials);
            double sllPoll = (size <= 10000) ? benchmarkSLLPoll(patients, trials) : -1.0;
            double fqPoll = benchmarkFQPoll(patients, trials);

            System.out.printf("   Poll (Serve N)       | PQ: %8.2f ms | SLL: %8s | 5-Buckets: %8.2f ms\n",
                    pqPoll, (sllPoll < 0 ? "O(N^2) Skip" : String.format("%.2f ms", sllPoll)), fqPoll);
            csvRows.add(new String[]{ String.valueOf(size), "Poll_N_Patients", f(pqPoll), (sllPoll < 0 ? "N/A" : f(sllPoll)), f(fqPoll) });

            System.out.println();
        }

        exportCSV(csvRows);
        System.out.println("Triage benchmark completed successfully.");
    }

    private static String f(double v) {
        return String.format("%.2f", v);
    }

    private static double benchmarkPQAdmit(List<Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            PriorityQueue<Patient> pq = new PriorityQueue<>(
                    Comparator.comparingInt(Patient::getSeverity).thenComparingLong(Patient::getArrival)
            );
            long start = System.nanoTime();
            for (Patient p : items) pq.add(p);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkPQPoll(List<Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            PriorityQueue<Patient> pq = new PriorityQueue<>(
                    Comparator.comparingInt(Patient::getSeverity).thenComparingLong(Patient::getArrival)
            );
            pq.addAll(items);
            long start = System.nanoTime();
            while (!pq.isEmpty()) pq.poll();
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkSLLAdmit(List<Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            SortedLinkedListTriage sll = new SortedLinkedListTriage();
            long start = System.nanoTime();
            for (Patient p : items) sll.admit(p);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkSLLPoll(List<Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            SortedLinkedListTriage sll = new SortedLinkedListTriage();
            for (Patient p : items) sll.admit(p);
            long start = System.nanoTime();
            while (sll.poll() != null);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkFQAdmit(List<Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            FiveQueueTriage fq = new FiveQueueTriage();
            long start = System.nanoTime();
            for (Patient p : items) fq.admit(p);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static double benchmarkFQPoll(List<Patient> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            FiveQueueTriage fq = new FiveQueueTriage();
            for (Patient p : items) fq.admit(p);
            long start = System.nanoTime();
            while (fq.poll() != null);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1_000_000.0;
    }

    private static void exportCSV(List<String[]> rows) {
        File dirResults = new File("Task06_HospitalTriage/results");
        if (!dirResults.exists()) dirResults = new File("CSA0916-192321030-Assignment2/Task06_HospitalTriage/results");
        File dirBench = new File("Benchmark_Results");
        if (!dirBench.exists()) dirBench = new File("CSA0916-192321030-Assignment2/Benchmark_Results");

        File[] targets = { new File(dirResults, "Q6_PriorityQueue_Benchmark.csv"), new File(dirBench, "Q6_PriorityQueue_Benchmark.csv") };
        for (File target : targets) {
            target.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                for (String[] r : rows) pw.println(String.join(",", r));
                System.out.println("Triage benchmark data exported to: " + target.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving CSV to " + target + ": " + e.getMessage());
            }
        }
    }
}

