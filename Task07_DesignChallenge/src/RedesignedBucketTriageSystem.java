package Task07_DesignChallenge.src;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Task 07: Redesigned Multi-Level Bucket Triage Engine
 * Architecture: Five Bounded Arrival Min-Heaps (One per discrete severity level 1..5)
 *
 * Mechanical Advantages:
 * 1. Monotonic Invariant: Each bucket heap is keyed SOLELY on arrivalTime.
 *    The root (peek()) is guaranteed to be the oldest patient in that severity tier.
 * 2. Guaranteed-Safe Early-Exit: If currentTime - root.arrivalTime < agingIntervalMs,
 *    no other patient in that bucket can possibly have waited >= agingIntervalMs.
 *    Early-exit break is mathematically proven NEVER to miss an eligible patient!
 * 3. Exact FIFO Ordering: Serving always polls root from the highest-priority non-empty
 *    bucket in O(log(N/5)) time, strictly preserving arrival order for equal severities.
 * 4. Fast O(log(N/5)) Re-heapification: Promoted patients are inserted into targetBucket
 *    in O(log(N/5)) time without list traversal or array copying.
 */
public class RedesignedBucketTriageSystem {

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

    @SuppressWarnings("unchecked")
    private final PriorityQueue<Patient>[] buckets = new PriorityQueue[5];
    private int size = 0;

    public RedesignedBucketTriageSystem() {
        for (int i = 0; i < 5; i++) {
            buckets[i] = new PriorityQueue<>(Comparator.comparingLong(p -> p.arrivalTime));
        }
    }

    // Direct enqueue to the appropriate severity bucket heap in O(log(N/5)) time
    public void admit(Patient p) {
        if (p.severity < 1 || p.severity > 5) {
            throw new IllegalArgumentException("Severity must be between 1 and 5");
        }
        buckets[p.severity - 1].add(p);
        size++;
    }

    // Serving: checks buckets 0 to 4 and polls the earliest arrival in O(log(N/5)) time
    public Patient poll() {
        for (int i = 0; i < 5; i++) {
            if (!buckets[i].isEmpty()) {
                size--;
                return buckets[i].poll();
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    /**
     * O(K log(N/5)) Guaranteed-Safe Early-Exit Aging Promotion:
     * Exploits the fact that each bucket heap's root is guaranteed to be the minimum arrival time.
     * If the root has not waited >= agingIntervalMs, then ALL other patients in that bucket
     * arrived later, so breaking early is 100% mathematically safe and CANNOT miss any eligible patient!
     */
    public int applyAging(long currentTime, long agingIntervalMs) {
        int promotedCount = 0;
        // Promote from Severity 5 down to 2 into the higher tier
        for (int sev = 4; sev >= 1; sev--) {
            PriorityQueue<Patient> currentBucket = buckets[sev];
            PriorityQueue<Patient> targetBucket = buckets[sev - 1];

            while (!currentBucket.isEmpty()) {
                Patient oldest = currentBucket.peek();
                long waited = currentTime - oldest.arrivalTime;
                if (waited >= agingIntervalMs) {
                    currentBucket.poll();
                    oldest.severity = sev; // 1-indexed target severity
                    targetBucket.add(oldest);
                    promotedCount++;
                } else {
                    // Mathematically proven safe: all other elements in this min-heap have arrival >= root,
                    // so no other patient can be eligible. Early exit is 100% sound!
                    break;
                }
            }
        }
        return promotedCount;
    }
}
