# Task 02: Music Playlist Editor (ArrayList vs. LinkedList vs. ArrayDeque)
**Course Code**: CSA0916 – Programming in Java  
**Student Name**: Hasin Banu K  
**Register Number**: 192321030  
**Assignment**: Assignment 2 – CO2  

## 1. Problem Context
A music streaming application allows users to build and manage playlists:
- Appending songs at the end.
- Inserting and deleting songs at specific positions (front, middle).
- Jumping directly to the $k$-th song (random access).
- Iterating through the entire playlist.
- Managing an "Up Next" queue where songs are pushed/popped from both ends.

This study benchmarks `ArrayList`, `LinkedList`, `ArrayDeque`, and a `Plain Array` with manual shifting across playlist sizes $N \in \{1,000, 10,000, 100,000\}$.

---

## 2. Experimental Benchmark Results

Raw benchmark data saved in `results/Q2_Collections_Benchmark.csv` and `Benchmark_Results/Q2_Collections_Benchmark.csv`:

| Playlist Size ($N$) | Operation | ArrayList ($\mu\text{s}$) | LinkedList ($\mu\text{s}$) | ArrayDeque ($\mu\text{s}$) | Plain Array ($\mu\text{s}$) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1,000** | Add at End (1,000 items) | 111.10 | 152.39 | 105.63 | 53.14 |
| | Insert Front (1,000 ops) | 265.57 | 45.64 | 54.93 | 797.63 |
| | Insert Middle (1,000 ops) | 131.40 | 2,290.53 | N/A | 469.92 |
| | Remove Front (1,000 ops) | 156.33 | 65.22 | 65.86 | 345.93 |
| | Remove Middle (1,000 ops) | 116.30 | 728.54 | N/A | 236.28 |
| | Get $k$-th (10,000 lookups) | 279.43 | 5,808.70 | N/A | 116.08 |
| | Full Iteration (1,000 items) | 39.47 | 83.67 | 86.29 | 11.03 |
| **10,000** | Add at End (10,000 items) | 603.92 | 674.93 | 719.39 | 444.50 |
| | Insert Front (1,000 ops) | 944.84 | 28.93 | 49.42 | 4,857.61 |
| | Insert Middle (1,000 ops) | 511.98 | 14,720.92 | N/A | 2,500.42 |
| | Remove Front (1,000 ops) | 862.96 | 41.23 | 42.23 | 4,412.21 |
| | Remove Middle (1,000 ops) | 477.75 | 12,997.59 | N/A | 2,271.21 |
| | Get $k$-th (10,000 lookups) | 192.72 | 65,668.16 | N/A | 148.62 |
| | Full Iteration (10,000 items) | 399.86 | 441.49 | 439.29 | 111.53 |
| **100,000** | Add at End (100,000 items) | 1,411.02 | 1,719.83 | 1,470.22 | 1,566.07 |
| | Insert Front (500 ops) | 6,226.03 | 12.02 | 49.34 | 9,364.60 |
| | Insert Middle (500 ops) | 2,992.42 | 67,000.29 | N/A | 4,534.58 |
| | Remove Front (500 ops) | 6,200.39 | 18.40 | 16.83 | 9,447.70 |
| | Remove Middle (500 ops) | 3,027.65 | 65,193.57 | N/A | 4,718.04 |
| | Get $k$-th (10,000 lookups) | 84.98 | 661,202.17 | N/A | 45.65 |
| | Full Iteration (100,000 items)| 934.37 | 1,007.23 | 1,290.25 | 402.36 |

---

## 3. Engineering Analysis

### When is ArrayList an Appropriate Engineering Choice?
1. **Random Access ($O(1)$)**: At $N=100,000$, 10,000 random lookups took **84.98 $\mu\text{s}$** in `ArrayList` compared to **661,202.17 $\mu\text{s}$** (~661 ms) in `LinkedList` — `ArrayList` is over **7,700 times faster**.
2. **Sequential Iteration**: Full playlist iteration is fastest in `ArrayList` and `Plain Array` due to spatial locality and CPU cache line prefetching (cache-friendly contiguous memory blocks).
3. **Appends at the End**: Amortized $O(1)$ due to $1.5\times$ growth factor in HotSpot JVM.

### When to Stop Recommending ArrayList?
When the dominant workload consists of high-frequency insertions or deletions at the **front (index 0)** of large lists ($N > 10,000$). Each front insertion forces an $O(N)$ `System.arraycopy` memory block shift. In our benchmark, 500 front insertions at $N=100,000$ took **6,226 $\mu\text{s}$** in `ArrayList` versus **12.02 $\mu\text{s}$** in `LinkedList` and **49.34 $\mu\text{s}$** in `ArrayDeque`.

### The "LinkedList is Faster for Middle Insertions" Fallacy
A classic textbook misconception claims `LinkedList` is better for arbitrary insertions. Our empirical data refutes this:
- At $N=100,000$, 500 middle insertions took **2,992.42 $\mu\text{s}$** in `ArrayList`, but **67,000.29 $\mu\text{s}$** in `LinkedList` (**22.4 times slower**).
- **Reason**: Although updating pointers is $O(1)$, finding the middle node requires traversing $N/2$ heap-scattered pointers ($O(N)$), incurring severe CPU cache misses. Modern CPU memory controllers can shift contiguous memory (`System.arraycopy`) orders of magnitude faster than pointer chasing across fragmented heap memory.

---

## 4. Changed Condition: The "Up Next" Queue and ArrayDeque
For an "Up Next" queue requiring frequent insertions and removals at both ends:
- **`ArrayDeque` is the optimal choice**:
  1. It implements a circular array with head and tail pointers.
  2. `addFirst`, `addLast`, `removeFirst`, `removeLast` are all amortized $O(1)$ without any element shifting.
  3. Unlike `LinkedList`, `ArrayDeque` allocates **zero node objects**, saving significant memory and eliminating GC pressure.
  4. In our benchmarks, `ArrayDeque` front insertions took **49.34 $\mu\text{s}$** and front removals took **16.83 $\mu\text{s}$** at $N=100,000$, closely matching `LinkedList` while consuming $\approx \frac{1}{4}$ the memory.

---

## 5. Complexity & Memory Comparison Table

| Operation | ArrayList | LinkedList | ArrayDeque | Plain Array (Manual Shift) |
| :--- | :--- | :--- | :--- | :--- |
| **Add at End** | $O(1)$ amortized | $O(1)$ | $O(1)$ amortized | $O(1)$ amortized (with resizing) |
| **Insert at Front** | $O(N)$ (shifts $N$ elements) | $O(1)$ | $O(1)$ amortized | $O(N)$ (manual `System.arraycopy`) |
| **Insert in Middle**| $O(N)$ (shifts $N/2$ elements)| $O(N)$ ($N/2$ pointer hops) | N/A (unsupported) | $O(N)$ (shifts $N/2$ elements) |
| **Remove from Front**| $O(N)$ (shifts $N-1$ elements)| $O(1)$ | $O(1)$ amortized | $O(N)$ (manual `System.arraycopy`) |
| **Remove from Middle**| $O(N)$ (shifts $N/2$ elements)| $O(N)$ ($N/2$ pointer hops) | N/A (unsupported) | $O(N)$ (shifts $N/2$ elements) |
| **Get $k$-th element**| $O(1)$ (direct index) | $O(N)$ ($\min(k, N-k)$ hops)| N/A (unsupported) | $O(1)$ (direct pointer index) |
| **Iteration** | $O(N)$ (cache-optimal) | $O(N)$ (cache-poor) | $O(N)$ (cache-optimal)| $O(N)$ (hardware SIMD vectorizable)|
| **Space Per Element**| 4–8 bytes (ref slot) + unused capacity | 24–32 bytes (`Node` header, prev, next, item)| 4–8 bytes (ref slot in power-of-2 ring)| 4–8 bytes (exact ref slot) |

---

## 6. Compilation and Execution
```bash
# From repository root:
javac -d . Task02_ListComparison/src/*.java
java Task02_ListComparison.src.PlaylistBenchmark
```

