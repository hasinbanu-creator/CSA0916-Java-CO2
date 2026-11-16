# Task 04: Hospital Pharmacy Stock Lookup (HashMap vs. TreeMap vs. LinkedHashMap)
**Course Code**: CSA0916 – Programming in Java  
**Student Name**: Hasin Banu K  
**Register Number**: 192321030  
**Assignment**: Assignment 2 – CO2  

## 1. Problem Context
A hospital pharmacy manages tens of thousands of medicines identified by alphanumeric codes (e.g., `MED-10492`). 
System requirements:
1. **High-frequency lookups**: Pharmacists scan and look up stock hundreds of thousands of times daily during prescription dispensing.
2. **Evening Report 1**: All medicines listed in alphabetical / numerical code order (sorted order).
3. **Evening Report 2**: All medicines listed in the exact chronological order in which they were added/restocked (insertion order).

---

## 2. Experimental Benchmark Results

Raw benchmark data saved in `results/Q4_Map_Benchmark.csv` and `Benchmark_Results/Q4_Map_Benchmark.csv`:

| Inventory Size ($N$) | Metric | HashMap | TreeMap | LinkedHashMap |
| :--- | :--- | :--- | :--- | :--- |
| **1,000** | Insertion Time (ms) | 0.12 ms | 0.92 ms | 0.21 ms |
| | 100,000 Lookups (ms) | 3.39 ms | 16.57 ms | 2.88 ms |
| | Sorted Code Report (ms) | 0.87 ms | 0.13 ms | 0.58 ms |
| | Insertion Order Report (ms)| N/A | N/A | 0.19 ms |
| **10,000** | Insertion Time (ms) | 1.05 ms | 4.42 ms | 0.66 ms |
| | 100,000 Lookups (ms) | 3.06 ms | 21.71 ms | 2.98 ms |
| | Sorted Code Report (ms) | 3.80 ms | 0.91 ms | 3.59 ms |
| | Insertion Order Report (ms)| N/A | N/A | 0.31 ms |
| **100,000** | Insertion Time (ms) | 11.00 ms | 46.65 ms | 9.06 ms |
| | 100,000 Lookups (ms) | 3.27 ms | 41.69 ms | 3.15 ms |
| | Sorted Code Report (ms) | 24.94 ms | 3.96 ms | 30.11 ms |
| | Insertion Order Report (ms)| N/A | N/A | 1.04 ms |

*(Averaged across 5 measured iterations on Java 21 LTS).*

---

## 3. Output Order Verification
Testing key iteration on insertion sequence: `[MED-9002, MED-1005, MED-4500, MED-2200]`
- **`HashMap`**: `[MED-2200, MED-1005, MED-4500, MED-9002]` — Arbitrary order dictated by bucket hash indices `(hash & table.length - 1)`.
- **`TreeMap`**: `[MED-1005, MED-2200, MED-4500, MED-9002]` — Sorted lexicographical order maintained by Red-Black binary search tree.
- **`LinkedHashMap`**: `[MED-9002, MED-1005, MED-4500, MED-2200]` — Exact insertion order preserved via internal doubly-linked list pointers (`before`/`after`).

---

## 4. Engineering Decisions and Scalability Inquiry

### Acceptable Response Time Definition
For a clinical dispensing terminal with bar code scanners, an acceptable lookup latency per scan is **under 1 microsecond ($< 1\ \mu\text{s}$)**. 
- In our benchmark, `HashMap` and `LinkedHashMap` serviced 100,000 lookups in **~3.2 ms**, averaging **~32 nanoseconds per lookup** ($0.032\ \mu\text{s}$), exceeding the requirement by a factor of 30.
- `TreeMap` took **~41.7 ms** for 100,000 lookups (~$0.42\ \mu\text{s}$ per lookup). While still sub-microsecond, it is **13x slower** due to $O(\log_2 N)$ tree traversal comparisons and CPU cache misses from pointer chasing.

### Recommended Production Deployment: `LinkedHashMap`
- **Why `LinkedHashMap` is the winning design**:
  1. Lookup speed is identical to `HashMap` ($O(1)$ amortized, ~31 ns).
  2. Naturally satisfies **Report 2 (Insertion Order)** in $O(N)$ time (1.04 ms) with zero extra sorting.
  3. Generating **Report 1 (Sorted Order)** once per day takes only **~30 ms** for 100,000 items (extract keys and sort).
  4. Extra cost: Only 2 additional 64-bit pointers (`before` and `after`) per entry (16 bytes), a negligible trade-off for retaining insertion chronology.

### Why `Hashtable` is Obsolete
1. **Unnecessary Synchronization**: Every method in `Hashtable` (`get`, `put`, `remove`) is guarded by a method-level intrinsic lock (`synchronized`). In modern systems, this introduces crippling thread contention.
2. **Replaced by Better Standards**: For single-threaded workloads, `HashMap` is faster; for concurrent multi-threaded workloads, `ConcurrentHashMap` provides lock striping / non-blocking bucket CAS operations.
3. **No Null Support**: Does not permit `null` keys or values.
4. **Legacy API**: Uses outdated `Enumeration` instead of the modern `Iterator` / Stream API.

---

## 5. Alternative Concept: Sorted Array with `Arrays.binarySearch()`

| Operation | Sorted Array + Binary Search | HashMap | TreeMap | LinkedHashMap |
| :--- | :--- | :--- | :--- | :--- |
| **Lookup (`get`)** | $O(\log N)$ comparisons | $O(1)$ amortized | $O(\log N)$ comparisons | $O(1)$ amortized |
| **Insertion (`put`)** | $O(N)$ (shifts array elements) | $O(1)$ amortized | $O(\log N)$ tree insert | $O(1)$ amortized |
| **Sorted Code Report**| $O(N)$ direct linear scan | $O(N \log N)$ (must sort) | $O(N)$ in-order traversal | $O(N \log N)$ (must sort) |
| **Insertion Report** | Impossible (loses order) | Impossible (loses order) | Impossible (loses order) | $O(N)$ native traversal |
| **Memory Overhead** | **Lowest** (1 raw reference array)| High (Node object + hash) | High (Entry object + 3 ptrs + color) | Highest (Entry + hash + before/after ptrs) |

---

## 6. Compilation and Execution
```bash
# From repository root:
javac -d . Task04_MapComparison/src/*.java
java Task04_MapComparison.src.PharmacyInventoryBenchmark
java Task04_MapComparison.src.SortedArrayInventoryDemo
```

