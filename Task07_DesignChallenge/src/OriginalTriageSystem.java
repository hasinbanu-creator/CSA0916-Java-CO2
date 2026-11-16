package Task07_DesignChallenge.src;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Task 07: Original Solution (from Q6)
 * Uses Java's PriorityQueue with compound comparator (severity, arrival).
 * Demonstrates the O(N log N) aging update bottleneck.
 */
public class OriginalTriageSystem {

    public static class Patient {
        public final String name;
        public int severity;
        public final long arrivalTime;

        public Patient(String name, int severity, long arrivalTime) {
            this.name = name;
            this.severity = severity;
            this.arrivalTime = arrivalTime;
        }

        @Override
        public String toString() {
            return name + "(S" + severity + ")";
        }
    }

    private final PriorityQueue<Patient> queue;

    public OriginalTriageSystem() {
        this.queue = new PriorityQueue<>(
                Comparator.comparingInt((Patient p) -> p.severity)
                          .thenComparingLong(p -> p.arrivalTime)
        );
    }

    public void admit(Patient p) {
        queue.add(p); // O(log N)
    }

    public Patient poll() {
        return queue.poll(); // O(log N)
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    /**
     * Aging update in PriorityQueue:
     * Because PriorityQueue does not support in-place key updates,
     * all elements must be drained and rebuilt to maintain the heap invariant!
     * Complexity: O(N log N)
     */
    public void applyAging(long currentTime, long agingIntervalMs) {
        List<Patient> list = new ArrayList<>(queue);
        queue.clear();
        for (Patient p : list) {
            long waited = currentTime - p.arrivalTime;
            int promotions = (int) (waited / agingIntervalMs);
            if (promotions > 0) {
                p.severity = Math.max(1, p.severity - promotions);
            }
            queue.add(p); // Re-inserting triggers O(log N) sift-up
        }
    }
}

