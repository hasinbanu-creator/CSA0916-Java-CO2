# Task 05: Generic Ranking Component (Generics, Comparable, Comparator)
**Course Code**: CSA0916 – Programming in Java  
**Student Name**: Hasin Banu K  
**Register Number**: 192321030  
**Assignment**: Assignment 2 – CO2  

## 1. Problem Context
An inter-college sports tournament requires a generic leaderboard component to rank teams based on multi-level criteria:
1. **Points** (Highest first $\to$ Descending)
2. **Goal Difference** (Highest first $\to$ Descending)
3. **Team Name** (Alphabetical A–Z $\to$ Ascending)

The leaderboard must be generic to support ranking players, colleges, and quiz participants.

---

## 2. Complete the Code Verification

### Expected Output:
`[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`

### Actual Execution Output:
```
=== INTER-COLLEGE SPORTS LEADERBOARD DEMO ===
[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]
```

### Why Bisons Ranks Above Panthers:
- Eagles has 9 points and $+6$ goal difference $\to$ Ranks 1st.
- Both Bisons and Panthers have **9 points** (1st level tie).
- Both Bisons and Panthers have **$+4$ goal difference** (2nd level tie).
- The tie is broken by the **3rd level criterion (Team Name in alphabetical order)**:
  - Lexicographically, `"Bisons".compareTo("Panthers") < 0` because `'B'` precedes `'P'`.
  - Therefore, **Bisons is ranked 2nd**, and **Panthers is ranked 3rd**.

---

## 3. Engineering Decisions and Correctness Principles

### Defensive Copying in `ranking()`
`Leaderboard.ranking()` creates a shallow defensive copy before sorting:
```java
List<T> sortedCopy = new ArrayList<>(items);
sortedCopy.sort(order);
return sortedCopy;
```
- **Rationale**: If `items` were sorted in-place, calling `ranking()` would mutate the internal collection state, destroying arrival chronology and violating the principle of query idempotence (queries should not cause side effects).

### Comparable vs. Comparator
1. **Comparable (`compareTo`)**: Defines the *natural ordering* of an object intrinsic to the class. It enforces a single, fixed comparison logic inside the class declaration.
2. **Comparator (`compare`)**: Defines *extrinsic ordering strategies* external to the class. 
3. **Why Comparator is Superior for `Leaderboard<T>`**:
   - Adheres to the **Single Responsibility Principle (SRP)** and **Open/Closed Principle (OCP)**: The `Team` class simply models team attributes, while leaderboard ranking strategies can change (e.g., Fair Play points, head-to-head records) without modifying `Team`.
   - Allows entities from third-party libraries (which cannot be modified to implement `Comparable`) to be ranked.
   - Enables complete reusability for other types (`Player`, `College`, `QuizTeam`).

### Bounded Generics and Wildcards: `Comparator<? super T>`
The constructor accepts `Comparator<? super T>`:
- Following the **PECS rule (Producer Extends, Consumer Super)**: The comparator *consumes* instances of `T` to compare them.
- This allows a `Comparator<Participant>` to rank a `Leaderboard<Team>` where `Team extends Participant`.

---

## 4. Edge Cases and Faulty Comparator Demonstration

### Fault A: Integer Subtraction Overflow
Many novice developers write `(b.points - a.points)` instead of `Integer.compare(b.points, a.points)`:
- If `b.points = 2,000,000,000` and `a.points = -2,000,000,000`, the subtraction overflows 32-bit signed integer limits:
  $$2,000,000,000 - (-2,000,000,000) = 4,000,000,000 \to -294,967,296$$
- The sign flips from positive to negative, completely inverting the leaderboard ranking.
- **Solution**: Always use `Integer.compare(x, y)` or `Comparator.comparingInt()`.

### Fault B: Missing Tie-Breaker
A comparator omitting the name tie-breaker reports `compare(Bisons, Panthers) == 0`.
- In a `TreeSet` or binary search, one of the two teams will be discarded as a duplicate!
- In sorting, relative order becomes non-deterministic across platforms.

### Transitivity and Consistency Verification
Our comparator satisfies:
1. **Transitivity**: If $A \succ B$ and $B \succ C$, then $A \succ C$.
2. **Antisymmetry**: $\text{sgn}(\text{compare}(A, B)) = -\text{sgn}(\text{compare}(B, A))$.
3. **Reflexivity**: $\text{compare}(A, A) == 0$.

---

## 5. Alternative Concept: Comparator Chaining vs. Three Stable Sorts

| Metric | Chained Comparator (1 Pass) | Three Successive Stable Sorts |
| :--- | :--- | :--- |
| **Algorithm** | Single TimSort pass using composite comparator | 3 distinct TimSort passes (Name $\to$ GD $\to$ Points) |
| **Time Complexity** | $1 \times O(N \log N)$ comparisons | $3 \times O(N \log N)$ comparisons |
| **Measured Time ($N=50,000$)**| **17.77 ms** | **30.83 ms** (~1.7x slower) |
| **Array Allocations** | 1 working array | 3 working array passes |
| **Reusability** | Clean, declarative lambda chain | Imperative, sequence-dependent, error-prone |

---

## 6. Compilation and Execution
```bash
# From repository root:
javac -d . Task05_GenericLeaderboard/src/*.java
java Task05_GenericLeaderboard.src.LeaderboardDemo
java Task05_GenericLeaderboard.src.LeaderboardTests
java Task05_GenericLeaderboard.src.StableSortVsChainingBenchmark
```

