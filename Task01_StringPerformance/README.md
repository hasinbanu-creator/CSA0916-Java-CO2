# Task 01: Building Text Under Load (String, StringBuilder, StringBuffer)
**Course Code**: CSA0916 – Programming in Java  
**Student Name**: Hasin Banu K  
**Register Number**: 192321030  
**Assignment**: Assignment 2 – CO2  

## 1. Problem Context
A railway reservation system generates e-tickets for every booking by joining various fields (PNR, train details, passenger names, coach, berth, fares, etc.) into a printable text block. The legacy system used `+` concatenation inside a loop. This investigation compares `String (+)` in a loop, `StringBuilder`, and `StringBuffer` across three deliberately contrasting workloads.

---

## 2. Workload Design
1. **Small Workload**: 1 short ticket with 5 fields.
2. **Medium Workload**: 1,000 tickets with 20 fields each.
3. **Large Workload**: 10,000 tickets with 50 fields each.

---

## 3. Predictions Before Implementation
- **String (+)**: Because `String` objects are immutable in Java, each concatenation `ticket = ticket + field + "\n"` cannot modify the existing buffer in-place. Across $K$ fields in a loop, the JIT cannot optimize the entire loop into a single buffer, forcing new `StringBuilder` and `String` allocations on each step. Characters are repeatedly re-copied, leading to $O(K^2)$ character copying per ticket and generating $2 \times K$ intermediate String objects per ticket. Under large workloads (10,000 tickets $\times$ 50 fields = 1,000,000 intermediate objects), high GC pressure and significant CPU overhead are predicted.
- **StringBuilder**: Uses an unsynchronized, dynamically resizing internal `byte[]`/`char[]` buffer. Fields are appended in-place with amortized $O(1)$ cost ($O(K)$ total per ticket), generating zero intermediate String objects. It is predicted to be the fastest.
- **StringBuffer**: Employs the same dynamic buffer as `StringBuilder`, but every public method (e.g., `append()`, `toString()`) is `synchronized`. It will produce zero intermediate String objects but will exhibit slight synchronization overhead compared to `StringBuilder`.

---

## 4. Experimental Results

Raw benchmark data generated and saved to `results/Q1_String_Benchmark.csv` and `Benchmark_Results/Q1_String_Benchmark.csv`:

| Workload | Ticket Count | Fields / Ticket | Approach | Avg Time (ms) | Est. Intermediates | Approx Heap Delta (KB) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Small** | 1 | 5 | String (+) | ~0.073 ms | 10 | 14.3 KB |
| **Small** | 1 | 5 | StringBuilder | ~0.072 ms | 0 | 3.1 KB |
| **Small** | 1 | 5 | StringBuffer | ~0.068 ms | 0 | 3.0 KB |
| **Medium** | 1,000 | 20 | String (+) | ~2.489 ms | 40,000 | 1,392.1 KB |
| **Medium** | 1,000 | 20 | StringBuilder | ~0.882 ms | 0 | 1,729.2 KB |
| **Medium** | 1,000 | 20 | StringBuffer | ~0.924 ms | 0 | 1,754.7 KB |
| **Large** | 10,000 | 50 | String (+) | ~22.208 ms | 1,000,000 | 19,868.4 KB |
| **Large** | 10,000 | 50 | StringBuilder | ~6.084 ms | 0 | 4,161.1 KB |
| **Large** | 10,000 | 50 | StringBuffer | ~6.604 ms | 0 | 20,239.2 KB |

*(Timings obtained from 5 warm-up iterations and 10 measured runs averaged on Java 21 LTS).*

---

## 5. Memory Measurement Limitations
`Runtime.getRuntime().totalMemory() - freeMemory()` was measured before and after execution with explicit calls to `System.gc()`. However, these measurements carry known JVM limitations:
1. `System.gc()` is merely a hint to the JVM, not a guarantee that full garbage collection has completed.
2. Modern HotSpot JVMs employ generational collectors (G1 / ZGC) and Thread-Local Allocation Buffers (TLABs), meaning small allocations may not immediately reflect in heap-wide totals.
3. JIT compiler optimizations (such as escape analysis and scalar replacement) may elide heap allocations for non-escaping objects.

---

## 6. Design Decision: Multithreaded Booking Server
- **Per-Ticket Generation**: **`StringBuilder`** is strongly recommended. Ticket generation is thread-confined (the ticket object is created, populated, and serialized within the calling thread's stack frame). Since no other thread accesses this builder, thread synchronization is unnecessary. Using `StringBuffer` here would incur pointless monitor acquisition/release penalties.
- **Shared Booking Log**: **`StringBuffer`** or a dedicated concurrent logging structure (such as `ConcurrentLinkedQueue<String>` or an asynchronous SLF4J/Logback appender) must be used. Multiple worker threads appending audit records to a shared log require mutual exclusion to prevent race conditions and interleaved buffer corruption.

---

## 7. Alternative Concepts & Complexity Analysis
- **`String.join("\n", fields)`**: Internally delegates to `StringJoiner`. Computes the total required capacity before allocating, preventing repeated resizing. Clean and expressive for delimiter-separated text.
- **`StringJoiner`**: Provides prefix and suffix wrapping (e.g. headers and footers). Internally manages a `StringBuilder` or char array.
- **`String.format()`**: Expressive for formatted tabular text, but incurs regex/format specifier parsing overhead at runtime, making it 5x–10x slower for high-throughput loops.

### Complexity Comparison:
- **String (+) in loop**:
  - Time Complexity: $O(N \cdot K^2)$ characters copied, where $N$ is tickets and $K$ is fields per ticket.
  - Space Complexity: $O(N \cdot K^2)$ intermediate string bytes allocated across the loop before GC.
- **StringBuilder / StringBuffer**:
  - Time Complexity: $O(N \cdot K)$ amortized character appends.
  - Space Complexity: $O(N \cdot K)$ total memory, with 0 intermediate String objects.

---

## 8. Compilation and Execution
```bash
# From repository root:
javac -d . Task01_StringPerformance/src/*.java
java Task01_StringPerformance.src.RailwayTicketBenchmark
java Task01_StringPerformance.src.AlternativeStringMethodsDemo
```

