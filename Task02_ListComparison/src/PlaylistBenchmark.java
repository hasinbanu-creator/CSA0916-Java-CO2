package Task02_ListComparison.src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Task 02: Music Playlist Editor Benchmark
 * Compares ArrayList, LinkedList, ArrayDeque, and Plain Array with manual shifting
 * across playlist sizes: 1,000, 10,000, and 100,000 elements.
 */
public class PlaylistBenchmark {

    public static class Song {
        final int id;
        final String title;
        final String artist;
        final int durationSeconds;

        public Song(int id, String title, String artist, int durationSeconds) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.durationSeconds = durationSeconds;
        }

        @Override
        public String toString() {
            return title + " - " + artist + " (" + durationSeconds + "s)";
        }
    }

    // Helper to generate sample songs
    public static List<Song> generateSongs(int count) {
        List<Song> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(new Song(i, "Track_" + i, "Artist_" + (i % 20), 180 + (i % 120)));
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 02: MUSIC PLAYLIST EDITOR BENCHMARK");
        System.out.println("  Comparing ArrayList, LinkedList, ArrayDeque, and Plain Array");
        System.out.println("================================================================================\n");

        int[] sizes = { 1000, 10000, 100000 };
        int trials = 5;

        List<String[]> csvRows = new ArrayList<>();
        csvRows.add(new String[]{ "ListSize", "Operation", "ArrayList_us", "LinkedList_us", "ArrayDeque_us", "PlainArray_us" });

        for (int size : sizes) {
            System.out.println(">>> Testing Playlist Size: N = " + String.format("%,d", size));
            List<Song> baseSongs = generateSongs(size);

            // 1. Add at End (N insertions)
            double alAddEnd = benchmarkAddEndArrayList(baseSongs, trials);
            double llAddEnd = benchmarkAddEndLinkedList(baseSongs, trials);
            double adAddEnd = benchmarkAddEndArrayDeque(baseSongs, trials);
            double paAddEnd = benchmarkAddEndPlainArray(baseSongs, trials);
            printRow("Add at End (" + size + " items)", alAddEnd, llAddEnd, adAddEnd, paAddEnd);
            csvRows.add(new String[]{ String.valueOf(size), "Add_End", f(alAddEnd), f(llAddEnd), f(adAddEnd), f(paAddEnd) });

            // Number of operations for positional tests (to avoid minutes-long O(N^2) hangs in LinkedList)
            int opCount = (size <= 10000) ? 1000 : 500;

            // 2. Insert at Front (opCount times)
            double alInsertFront = benchmarkInsertFrontArrayList(baseSongs, opCount, trials);
            double llInsertFront = benchmarkInsertFrontLinkedList(baseSongs, opCount, trials);
            double adInsertFront = benchmarkInsertFrontArrayDeque(baseSongs, opCount, trials);
            double paInsertFront = benchmarkInsertFrontPlainArray(baseSongs, opCount, trials);
            printRow("Insert Front (" + opCount + " ops)", alInsertFront, llInsertFront, adInsertFront, paInsertFront);
            csvRows.add(new String[]{ String.valueOf(size), "Insert_Front_" + opCount, f(alInsertFront), f(llInsertFront), f(adInsertFront), f(paInsertFront) });

            // 3. Insert in Middle (opCount times)
            double alInsertMid = benchmarkInsertMiddleArrayList(baseSongs, opCount, trials);
            double llInsertMid = benchmarkInsertMiddleLinkedList(baseSongs, opCount, trials);
            double adInsertMid = -1.0; // Unsupported
            double paInsertMid = benchmarkInsertMiddlePlainArray(baseSongs, opCount, trials);
            printRow("Insert Middle (" + opCount + " ops)", alInsertMid, llInsertMid, adInsertMid, paInsertMid);
            csvRows.add(new String[]{ String.valueOf(size), "Insert_Middle_" + opCount, f(alInsertMid), f(llInsertMid), f(adInsertMid), f(paInsertMid) });

            // 4. Remove from Front (opCount times)
            double alRemoveFront = benchmarkRemoveFrontArrayList(baseSongs, opCount, trials);
            double llRemoveFront = benchmarkRemoveFrontLinkedList(baseSongs, opCount, trials);
            double adRemoveFront = benchmarkRemoveFrontArrayDeque(baseSongs, opCount, trials);
            double paRemoveFront = benchmarkRemoveFrontPlainArray(baseSongs, opCount, trials);
            printRow("Remove Front (" + opCount + " ops)", alRemoveFront, llRemoveFront, adRemoveFront, paRemoveFront);
            csvRows.add(new String[]{ String.valueOf(size), "Remove_Front_" + opCount, f(alRemoveFront), f(llRemoveFront), f(adRemoveFront), f(paRemoveFront) });

            // 5. Remove from Middle (opCount times)
            double alRemoveMid = benchmarkRemoveMiddleArrayList(baseSongs, opCount, trials);
            double llRemoveMid = benchmarkRemoveMiddleLinkedList(baseSongs, opCount, trials);
            double adRemoveMid = -1.0; // Unsupported
            double paRemoveMid = benchmarkRemoveMiddlePlainArray(baseSongs, opCount, trials);
            printRow("Remove Middle (" + opCount + " ops)", alRemoveMid, llRemoveMid, adRemoveMid, paRemoveMid);
            csvRows.add(new String[]{ String.valueOf(size), "Remove_Middle_" + opCount, f(alRemoveMid), f(llRemoveMid), f(adRemoveMid), f(paRemoveMid) });

            // 6. Access k-th Element (10,000 random lookups)
            int lookupCount = 10000;
            double alAccess = benchmarkGetKthArrayList(baseSongs, lookupCount, trials);
            double llAccess = benchmarkGetKthLinkedList(baseSongs, (size == 100000 ? 500 : lookupCount), trials);
            // Normalize llAccess if lookupCount scaled down
            if (size == 100000) llAccess = llAccess * (lookupCount / 500.0);
            double adAccess = -1.0; // ArrayDeque has no index-based get(k)
            double paAccess = benchmarkGetKthPlainArray(baseSongs, lookupCount, trials);
            printRow("Get k-th (" + lookupCount + " lookups)", alAccess, llAccess, adAccess, paAccess);
            csvRows.add(new String[]{ String.valueOf(size), "Get_Kth_" + lookupCount, f(alAccess), f(llAccess), f(adAccess), f(paAccess) });

            // 7. Iterate through entire collection
            double alIter = benchmarkIterateArrayList(baseSongs, trials);
            double llIter = benchmarkIterateLinkedList(baseSongs, trials);
            double adIter = benchmarkIterateArrayDeque(baseSongs, trials);
            double paIter = benchmarkIteratePlainArray(baseSongs, trials);
            printRow("Full Iteration (" + size + " items)", alIter, llIter, adIter, paIter);
            csvRows.add(new String[]{ String.valueOf(size), "Full_Iteration", f(alIter), f(llIter), f(adIter), f(paIter) });

            System.out.println();
        }

        exportCSV(csvRows);
        System.out.println("Benchmark completed successfully.");
    }

    private static String f(double val) {
        return val < 0 ? "N/A" : String.format("%.2f", val);
    }

    private static void printRow(String op, double al, double ll, double ad, double pa) {
        System.out.printf("   %-28s | AL: %10.2f us | LL: %10.2f us | AD: %10s | Array: %10.2f us\n",
                op, al, ll, (ad < 0 ? "N/A" : String.format("%.2f us", ad)), pa);
    }

    // --- Benchmarking Methods ---

    // 1. Add at end
    private static double benchmarkAddEndArrayList(List<Song> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            List<Song> list = new ArrayList<>();
            for (Song s : items) list.add(s);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkAddEndLinkedList(List<Song> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            List<Song> list = new LinkedList<>();
            for (Song s : items) list.add(s);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkAddEndArrayDeque(List<Song> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            Deque<Song> deque = new ArrayDeque<>();
            for (Song s : items) deque.addLast(s);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkAddEndPlainArray(List<Song> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            Song[] arr = new Song[10];
            int count = 0;
            for (Song s : items) {
                if (count == arr.length) {
                    Song[] next = new Song[arr.length * 2];
                    System.arraycopy(arr, 0, next, 0, arr.length);
                    arr = next;
                }
                arr[count++] = s;
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    // 2. Insert at Front
    private static double benchmarkInsertFrontArrayList(List<Song> items, int opCount, int trials) {
        Song dummy = new Song(-1, "InsertFront", "Test", 100);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            List<Song> list = new ArrayList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount; i++) list.add(0, dummy);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkInsertFrontLinkedList(List<Song> items, int opCount, int trials) {
        Song dummy = new Song(-1, "InsertFront", "Test", 100);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            LinkedList<Song> list = new LinkedList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount; i++) list.addFirst(dummy);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkInsertFrontArrayDeque(List<Song> items, int opCount, int trials) {
        Song dummy = new Song(-1, "InsertFront", "Test", 100);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            ArrayDeque<Song> deque = new ArrayDeque<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount; i++) deque.addFirst(dummy);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkInsertFrontPlainArray(List<Song> items, int opCount, int trials) {
        Song dummy = new Song(-1, "InsertFront", "Test", 100);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            Song[] arr = new Song[items.size() + opCount];
            for (int i = 0; i < items.size(); i++) arr[i] = items.get(i);
            int currentSize = items.size();

            long start = System.nanoTime();
            for (int i = 0; i < opCount; i++) {
                System.arraycopy(arr, 0, arr, 1, currentSize);
                arr[0] = dummy;
                currentSize++;
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    // 3. Insert in Middle
    private static double benchmarkInsertMiddleArrayList(List<Song> items, int opCount, int trials) {
        Song dummy = new Song(-2, "InsertMid", "Test", 100);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            List<Song> list = new ArrayList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount; i++) list.add(list.size() / 2, dummy);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkInsertMiddleLinkedList(List<Song> items, int opCount, int trials) {
        Song dummy = new Song(-2, "InsertMid", "Test", 100);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            LinkedList<Song> list = new LinkedList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount; i++) list.add(list.size() / 2, dummy);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkInsertMiddlePlainArray(List<Song> items, int opCount, int trials) {
        Song dummy = new Song(-2, "InsertMid", "Test", 100);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            Song[] arr = new Song[items.size() + opCount];
            for (int i = 0; i < items.size(); i++) arr[i] = items.get(i);
            int currentSize = items.size();

            long start = System.nanoTime();
            for (int i = 0; i < opCount; i++) {
                int mid = currentSize / 2;
                System.arraycopy(arr, mid, arr, mid + 1, currentSize - mid);
                arr[mid] = dummy;
                currentSize++;
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    // 4. Remove from Front
    private static double benchmarkRemoveFrontArrayList(List<Song> items, int opCount, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            List<Song> list = new ArrayList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount && !list.isEmpty(); i++) list.remove(0);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkRemoveFrontLinkedList(List<Song> items, int opCount, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            LinkedList<Song> list = new LinkedList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount && !list.isEmpty(); i++) list.removeFirst();
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkRemoveFrontArrayDeque(List<Song> items, int opCount, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            ArrayDeque<Song> deque = new ArrayDeque<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount && !deque.isEmpty(); i++) deque.removeFirst();
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkRemoveFrontPlainArray(List<Song> items, int opCount, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            Song[] arr = new Song[items.size()];
            for (int i = 0; i < items.size(); i++) arr[i] = items.get(i);
            int currentSize = items.size();

            long start = System.nanoTime();
            for (int i = 0; i < opCount && currentSize > 0; i++) {
                System.arraycopy(arr, 1, arr, 0, currentSize - 1);
                arr[--currentSize] = null;
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    // 5. Remove from Middle
    private static double benchmarkRemoveMiddleArrayList(List<Song> items, int opCount, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            List<Song> list = new ArrayList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount && !list.isEmpty(); i++) list.remove(list.size() / 2);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkRemoveMiddleLinkedList(List<Song> items, int opCount, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            LinkedList<Song> list = new LinkedList<>(items);
            long start = System.nanoTime();
            for (int i = 0; i < opCount && !list.isEmpty(); i++) list.remove(list.size() / 2);
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkRemoveMiddlePlainArray(List<Song> items, int opCount, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            Song[] arr = new Song[items.size()];
            for (int i = 0; i < items.size(); i++) arr[i] = items.get(i);
            int currentSize = items.size();

            long start = System.nanoTime();
            for (int i = 0; i < opCount && currentSize > 0; i++) {
                int mid = currentSize / 2;
                System.arraycopy(arr, mid + 1, arr, mid, currentSize - mid - 1);
                arr[--currentSize] = null;
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    // 6. Access k-th Element
    private static double benchmarkGetKthArrayList(List<Song> items, int lookupCount, int trials) {
        Random rand = new Random(42);
        int[] indices = rand.ints(lookupCount, 0, items.size()).toArray();
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            for (int idx : indices) {
                Song s = items.get(idx);
                if (s == null) System.out.print("");
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkGetKthLinkedList(List<Song> items, int lookupCount, int trials) {
        LinkedList<Song> ll = new LinkedList<>(items);
        Random rand = new Random(42);
        int[] indices = rand.ints(lookupCount, 0, items.size()).toArray();
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            for (int idx : indices) {
                Song s = ll.get(idx);
                if (s == null) System.out.print("");
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkGetKthPlainArray(List<Song> items, int lookupCount, int trials) {
        Song[] arr = items.toArray(new Song[0]);
        Random rand = new Random(42);
        int[] indices = rand.ints(lookupCount, 0, items.size()).toArray();
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            for (int idx : indices) {
                Song s = arr[idx];
                if (s == null) System.out.print("");
            }
            totalNs += (System.nanoTime() - start);
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    // 7. Iterate through entire collection
    private static double benchmarkIterateArrayList(List<Song> items, int trials) {
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            long sum = 0;
            for (Song s : items) sum += s.durationSeconds;
            totalNs += (System.nanoTime() - start);
            if (sum < 0) System.out.print("");
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkIterateLinkedList(List<Song> items, int trials) {
        LinkedList<Song> ll = new LinkedList<>(items);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            long sum = 0;
            for (Song s : ll) sum += s.durationSeconds;
            totalNs += (System.nanoTime() - start);
            if (sum < 0) System.out.print("");
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkIterateArrayDeque(List<Song> items, int trials) {
        ArrayDeque<Song> ad = new ArrayDeque<>(items);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            long sum = 0;
            for (Song s : ad) sum += s.durationSeconds;
            totalNs += (System.nanoTime() - start);
            if (sum < 0) System.out.print("");
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static double benchmarkIteratePlainArray(List<Song> items, int trials) {
        Song[] arr = items.toArray(new Song[0]);
        long totalNs = 0;
        for (int t = 0; t < trials; t++) {
            long start = System.nanoTime();
            long sum = 0;
            for (Song s : arr) sum += s.durationSeconds;
            totalNs += (System.nanoTime() - start);
            if (sum < 0) System.out.print("");
        }
        return (totalNs / (double) trials) / 1000.0;
    }

    private static void exportCSV(List<String[]> rows) {
        File dirResults = new File("Task02_ListComparison/results");
        if (!dirResults.exists()) dirResults = new File("CSA0916-192321030-Assignment2/Task02_ListComparison/results");
        File dirBench = new File("Benchmark_Results");
        if (!dirBench.exists()) dirBench = new File("CSA0916-192321030-Assignment2/Benchmark_Results");

        File[] targets = { new File(dirResults, "Q2_Collections_Benchmark.csv"), new File(dirBench, "Q2_Collections_Benchmark.csv") };
        for (File target : targets) {
            target.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                for (String[] r : rows) pw.println(String.join(",", r));
                System.out.println("Benchmark data exported to: " + target.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving CSV to " + target + ": " + e.getMessage());
            }
        }
    }
}

