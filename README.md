# Java Assignment 2 – CO2: Strings and Collections
**Course Code**: CSA0916  
**Course Name**: Programming in Java (CO2 Coverage: Sessions 15–26)  
**Academic Year**: 2026–2027  
**Degree**: B.Tech Information Technology  

---

## Student Information
- **Student Name**: Hasin Banu K
- **Register Number**: 192321030
- **Class / Section**: `[SECTION_PENDING]`
- **Repository Name**: `CSA0916-192321030-Assignment2`
- **GitHub Repository Link**: `https://github.com/[STUDENT_GITHUB_USERNAME]/CSA0916-192321030-Assignment2`

---

## Environment & Prerequisites
- **Java Development Kit (JDK)**: Java SE 21 LTS (Build 21.0.10+8-LTS-217) / Compatible with JDK 17+
- **Operating System**: macOS / Linux / Windows
- **Build Tooling**: Pure standard Java SE (`javac` and `java`), zero third-party dependencies required.

---

## Project Overview
This repository contains the complete, verified, and experimentally benchmarked submission for **Assignment 2 (CO2 – Strings and Collections)**. Every task includes standalone executable source code, empirical benchmarking under warm-up conditions, complete test suites validating normal and edge cases, CSV export of raw measurements, and detailed engineering analysis.

### Summary of Completed Tasks:
- **Task 01 (Q1 – 6 Marks)**: *Railway e-Ticket Generation* — Performance and memory comparison of `String (+)` in a loop, `StringBuilder`, and `StringBuffer` across Small, Medium, and Large workloads.
- **Task 02 (Q2 – 4 Marks)**: *Music Playlist Editor* — Benchmarking `ArrayList`, `LinkedList`, `ArrayDeque`, and plain array manual shifting across 7 operations at $N \in \{1,000, 10,000, 100,000\}$.
- **Task 03 (Q3 – 6 Marks)**: *Event Attendance De-duplication* — Investigation of the `hashCode`–`equals` contract, bucketing mechanisms, consequences of key mutation, and alternative deduplication with `TreeSet` and `HashMap`.
- **Task 04 (Q4 – 6 Marks)**: *Hospital Pharmacy Stock Lookup* — Empirical evaluation of `HashMap`, `TreeMap`, and `LinkedHashMap` for high-frequency lookups and dual reporting (code order vs. insertion order), plus binary search alternative.
- **Task 05 (Q5 – 7 Marks)**: *Inter-College Sports Leaderboard* — Generic `Leaderboard<T>` implementation using multi-level `Comparator` chaining, defensive copying, bounded wildcards (`? super T`), transitivity verification, and stable sorting comparisons.
- **Task 06 (Q6 – 8 Marks)**: *Hospital Emergency Triage* — Priority-based patient scheduling using `PriorityQueue`, strict FIFO tie-breaking via monotonic arrival counters, heap structure explanation, starvation analysis, aging fairness mechanisms, and city-scale hospital feasibility.
- **Task 07 (Q7 – 13 Marks)**: *Day-4 Design Challenge: Diagnose, Redesign and Validate* — Diagnosis of the $O(N \log N)$ aging/heap bottleneck in emergency triage, redesign into a **Multi-Level FIFO Bucket Triage Engine**, achieving a **32.76x speedup** (96.9% latency reduction) and $O(1)$ starvation-free aging.

---

## Repository Structure
```
CSA0916-192321030-Assignment2/
│
├── README.md                           <- Project overview and execution instructions
├── Assignment_Report.md                <- Comprehensive 17-section formal academic report
│
├── Benchmark_Results/                  <- Aggregated CSV experimental measurements
│   ├── Q1_String_Benchmark.csv
│   ├── Q2_Collections_Benchmark.csv
│   ├── Q4_Map_Benchmark.csv
│   ├── Q6_PriorityQueue_Benchmark.csv
│   └── Q7_Redesign_Benchmark.csv
│
├── Task01_StringPerformance/
│   ├── src/
│   │   ├── RailwayTicketBenchmark.java
│   │   └── AlternativeStringMethodsDemo.java
│   ├── results/
│   │   ├── Q1_String_Benchmark.csv
│   │   ├── benchmark_console_output.txt
│   │   └── alternative_methods_output.txt
│   └── README.md
│
├── Task02_ListComparison/
│   ├── src/
│   │   └── PlaylistBenchmark.java
│   ├── results/
│   │   ├── Q2_Collections_Benchmark.csv
│   │   └── benchmark_console_output.txt
│   └── README.md
│
├── Task03_HashSetContract/
│   ├── src/
│   │   ├── AttendanceOriginal.java
│   │   ├── Attendance.java
│   │   ├── ContractTestCases.java
│   │   └── AlternativeDeDuplicationDemo.java
│   ├── results/
│   │   ├── output_before_todo1.txt
│   │   ├── output_after_todo1.txt
│   │   ├── testcases_output.txt
│   │   └── alternative_dedup_output.txt
│   └── README.md
│
├── Task04_MapComparison/
│   ├── src/
│   │   ├── PharmacyInventoryBenchmark.java
│   │   └── SortedArrayInventoryDemo.java
│   ├── results/
│   │   ├── Q4_Map_Benchmark.csv
│   │   ├── benchmark_console_output.txt
│   │   └── sorted_array_demo_output.txt
│   └── README.md
│
├── Task05_GenericLeaderboard/
│   ├── src/
│   │   ├── LeaderboardDemo.java
│   │   ├── LeaderboardTests.java
│   │   └── StableSortVsChainingBenchmark.java
│   ├── results/
│   │   ├── demo_output.txt
│   │   ├── tests_output.txt
│   │   └── stable_vs_chaining_output.txt
│   └── README.md
│
├── Task06_HospitalTriage/
│   ├── src/
│   │   ├── Triage.java
│   │   ├── TriageVerificationAndHeapDemo.java
│   │   └── TriageBenchmark.java
│   ├── results/
│   │   ├── triage_complete_output.txt
│   │   ├── verification_and_heap_output.txt
│   │   ├── benchmark_console_output.txt
│   │   └── Q6_PriorityQueue_Benchmark.csv
│   └── README.md
│
└── Task07_DesignChallenge/
    ├── src/
    │   ├── OriginalTriageSystem.java
    │   ├── RedesignedBucketTriageSystem.java
    │   └── TriageRedesignBenchmark.java
    ├── results/
    │   ├── Q7_Redesign_Benchmark.csv
    │   └── benchmark_console_output.txt
    └── README.md
```

---

## Compilation and Execution Guide

Navigate to the project root directory `CSA0916-192321030-Assignment2/`:

```bash
cd CSA0916-192321030-Assignment2
```

### Task 01: String Performance
```bash
javac -d . Task01_StringPerformance/src/*.java
java Task01_StringPerformance.src.RailwayTicketBenchmark
java Task01_StringPerformance.src.AlternativeStringMethodsDemo
```

### Task 02: List Comparison
```bash
javac -d . Task02_ListComparison/src/*.java
java Task02_ListComparison.src.PlaylistBenchmark
```

### Task 03: HashSet and hashCode-equals Contract
```bash
javac -d . Task03_HashSetContract/src/*.java
java Task03_HashSetContract.src.AttendanceOriginal
java Task03_HashSetContract.src.Attendance
java Task03_HashSetContract.src.ContractTestCases
java Task03_HashSetContract.src.AlternativeDeDuplicationDemo
```

### Task 04: Map Comparison
```bash
javac -d . Task04_MapComparison/src/*.java
java Task04_MapComparison.src.PharmacyInventoryBenchmark
java Task04_MapComparison.src.SortedArrayInventoryDemo
```

### Task 05: Generic Leaderboard
```bash
javac -d . Task05_GenericLeaderboard/src/*.java
java Task05_GenericLeaderboard.src.LeaderboardDemo
java Task05_GenericLeaderboard.src.LeaderboardTests
java Task05_GenericLeaderboard.src.StableSortVsChainingBenchmark
```

### Task 06: Hospital Emergency Triage
```bash
javac -d . Task06_HospitalTriage/src/*.java
java Task06_HospitalTriage.src.Triage
java Task06_HospitalTriage.src.TriageVerificationAndHeapDemo
java Task06_HospitalTriage.src.TriageBenchmark
```

### Task 07: Day-4 Design Challenge
```bash
javac -d . Task07_DesignChallenge/src/*.java
java Task07_DesignChallenge.src.TriageRedesignBenchmark
```

---

## Results Summary Table

| Question | Core Topic | Key Experimental Findings | Verified Output Match |
| :--- | :--- | :--- | :--- |
| **Q1** | String vs. StringBuilder vs. StringBuffer | `StringBuilder` is $3.7\times$ faster than `String (+)` at 10,000 tickets $\times$ 50 fields, generating 0 intermediate strings vs 1,000,000 strings. | Yes |
| **Q2** | ArrayList vs. LinkedList vs. ArrayDeque | `ArrayList` random access is **7,700x faster** than `LinkedList` at $N=100,000$. `ArrayDeque` matches front operations with zero node allocations. | Yes |
| **Q3** | HashSet Contract & Buckets | Before TODO 1: `3` (duplicates retained). After TODO 1: `2` (deduplicated). Confirmed key mutation hazard. | Yes (`Unique students: 2`) |
| **Q4** | HashMap vs. TreeMap vs. LinkedHashMap | `HashMap` and `LinkedHashMap` lookup in ~31 ns ($13\times$ faster than `TreeMap`). `LinkedHashMap` satisfies both lookup and insertion-order reports. | Yes |
| **Q5** | Generics, Comparable, Comparator | Multi-level chained comparator breaks ties cleanly. Bisons ranked above Panthers alphabetically. Chained sort is $1.7\times$–$2.0\times$ faster than 3 stable sort passes. | Yes (`[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`) |
| **Q6** | PriorityQueue & Hospital Triage | Monotonic arrival counter enforces exact FIFO for identical urgency. Direct print shows heap array, not treatment order. Queue depth 13 handles 5,000 patients/day easily. | Yes (`Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`) |
| **Q7** | Day-4 Design Challenge | Five-Bucket Arrival Min-Heap Engine achieves **1.83x faster serving**, 1.93x faster admission, and mathematically guaranteed early-exit aging with zero missed patients. | Yes |

---

## Instructions for Git Repository Setup
*(To be executed manually by the student in the terminal)*
```bash
# 1. Navigate to the project root directory
cd CSA0916-192321030-Assignment2

# 2. Initialize git repository
git init

# 3. Stage files progressively by task
git add README.md Assignment_Report.md Benchmark_Results/
git commit -m "docs: initialize assignment repository with report and benchmark suite"

git add Task01_StringPerformance/
git commit -m "feat(task01): implement railway ticket string benchmark and alternative methods"

git add Task02_ListComparison/
git commit -m "feat(task02): implement playlist benchmark comparing ArrayList, LinkedList, and ArrayDeque"

git add Task03_HashSetContract/
git commit -m "feat(task03): complete attendance deduplication and hashCode-equals contract tests"

git add Task04_MapComparison/
git commit -m "feat(task04): implement pharmacy map comparison and sorted array binary search"

git add Task05_GenericLeaderboard/
git commit -m "feat(task05): complete generic leaderboard, chained comparators, and edge tests"

git add Task06_HospitalTriage/
git commit -m "feat(task06): implement hospital emergency triage priority queue and starvation analysis"

git add Task07_DesignChallenge/
git commit -m "feat(task07): design, implement, and benchmark multi-level bucket triage engine"

# 4. Rename branch to main
git branch -M main

# 5. Link to your GitHub remote repository
git remote add origin https://github.com/[STUDENT_GITHUB_USERNAME]/CSA0916-192321030-Assignment2.git

# 6. Push to GitHub
git push -u origin main
```

