# Task 06: Hospital Emergency Triage (PriorityQueue, FIFO & Fairness)
**Course Code**: CSA0916 – Programming in Java  
**Student Name**: Hasin Banu K  
**Register Number**: 192321030  
**Assignment**: Assignment 2 – CO2  

## 1. Problem Context
An emergency department classifies arriving patients by severity level:
- **Severity 1**: Critical (immediate life resuscitation)
- **Severity 5**: Minor (non-urgent)
- **Rule 1**: Lower severity number must be treated first.
- **Rule 2**: Patients with identical severity must be treated strictly in arrival order (FIFO).

---

## 2. Complete the Code Verification

### Expected Output:
`Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`

### Actual Execution Output:
```
=== HOSPITAL EMERGENCY TRIAGE SYSTEM ===
Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)
```

---

## 3. Engineering Mechanics

### Why Plain Queues (LinkedList / ArrayDeque) are Insufficient
A plain `Queue` enforces strict FIFO ordering regardless of clinical urgency. A cardiac arrest patient arriving at minute 10 would be trapped behind 50 minor flu patients who arrived at minute 5. To find the critical patient, doctor staff would need to scan the entire queue ($O(N)$ inspection), creating dangerous medical delays.

### How Arrival Counter Preserves FIFO for Identical Severities
`PriorityQueue` is backed by a binary min-heap which is **not stable** by itself. During sift-up and sift-down operations, elements with equal priority can swap relative order.
By introducing an auto-incrementing `arrival` counter and chaining the comparator:
```java
Comparator.comparingInt(Patient::getSeverity)
          .thenComparingLong(Patient::getArrival)
```
Every patient receives a unique, strictly monotonic key pair `(severity, arrival)`. No two patients ever compare as equal (`0`), turning the partial order into a strict total order and guaranteeing exact FIFO tie-breaking.

### Why `System.out.println(queue)` Does NOT Show Treatment Order
A `PriorityQueue` stores elements in an array satisfying the **heap property**:
$$\text{heap}[i] \le \text{heap}[2i + 1] \quad \text{and} \quad \text{heap}[i] \le \text{heap}[2i + 2]$$
This is a **partial order**, not a fully sorted sequence. Only the root (`index 0`) is guaranteed to be the minimum. When `System.out.println(queue)` is called, it iterates through raw array indices without extracting. To observe true treatment order, elements must be sequentially retrieved via `poll()`, which extracts the root and reheapifies the remaining elements in $O(\log N)$ time.

---

## 4. Benchmark Results: PriorityQueue vs. Sorted LinkedList vs. 5-Queue Buckets

Raw benchmark data saved in `results/Q6_PriorityQueue_Benchmark.csv` and `Benchmark_Results/Q6_PriorityQueue_Benchmark.csv`:

| Queue Size ($N$) | Operation | PriorityQueue | Sorted LinkedList | 5-Queue Buckets |
| :--- | :--- | :--- | :--- | :--- |
| **1,000** | Admit (Enqueue $N$) | 0.38 ms | 3.42 ms | **0.13 ms** |
| | Poll (Serve $N$) | 0.55 ms | 0.05 ms | **0.09 ms** |
| **10,000** | Admit (Enqueue $N$) | 0.91 ms | 150.98 ms | **0.65 ms** |
| | Poll (Serve $N$) | 2.44 ms | 0.19 ms | **0.36 ms** |
| **100,000** | Admit (Enqueue $N$) | 4.38 ms | $O(N^2)$ (Skipped) | **1.69 ms** |
| | Poll (Serve $N$) | 27.90 ms | $O(N^2)$ (Skipped) | **0.62 ms** |

---

## 5. Inquiry & Deployment Evaluation

### Starvation and Aging Fairness Mechanism
- **The Problem**: If a hospital experiences continuous waves of Severity 1–3 emergencies, Severity 5 patients will starve indefinitely in the waiting lounge.
- **Fair Aging Policy**:
  $$\text{Effective Severity} = \max\left(1, \ \text{Base Severity} - \left\lfloor \frac{\text{Wait Time}}{\text{Aging Threshold}} \right\rfloor\right)$$
  For example, with an aging threshold of 30 minutes, an S5 patient who has waited 120 minutes is escalated to S1, ensuring medical attention.

### City Hospital Scale Evaluation (5,000 Patients / Day)
- **Daily Volume**: 5,000 admits + 5,000 polls = 10,000 total queue operations per day.
- **Peak Heap Depth**: Max tree height $\lceil \log_2(5000) \rceil = 13$ comparisons per heapify.
- **Total CPU Time**: $\approx 10,000 \times 100\ \text{ns} = 1.0\ \text{ms}$ of CPU time for the entire 24-hour day.
- **Memory Footprint**: 5,000 patient nodes consume $\approx 160\ \text{KB}$ heap space.
- **Verdict**: The standard Java `PriorityQueue` easily satisfies a 5,000 patient/day hospital workload.

---

## 6. Compilation and Execution
```bash
# From repository root:
javac -d . Task06_HospitalTriage/src/*.java
java Task06_HospitalTriage.src.Triage
java Task06_HospitalTriage.src.TriageVerificationAndHeapDemo
java Task06_HospitalTriage.src.TriageBenchmark
```

