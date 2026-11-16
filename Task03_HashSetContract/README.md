# Task 03: HashSet and the hashCode–equals Contract
**Course Code**: CSA0916 – Programming in Java  
**Student Name**: Hasin Banu K  
**Register Number**: 192321030  
**Assignment**: Assignment 2 – CO2  

## 1. Problem Context
A college fest records student attendance at gate scanners. A `HashSet<Student>` is used to filter out duplicate scans so that each student is counted only once. 

The original `Student` class implemented `equals()` based on `regNo` but did **not** override `hashCode()`.

---

## 2. Execution Evidence: Before vs. After Fixing TODO 1

### Output Before Completing TODO 1:
```
=== ATTENDANCE PROGRAM (BEFORE FIXING TODO 1) ===
Unique students: 3
Students in set: [192311001 Arun, 192311001 Arun, 192311002 Divya]
```

### Output After Completing TODO 1:
```
=== ATTENDANCE PROGRAM (AFTER FIXING TODO 1) ===
Unique students: 2
Students in set: [192311001 Arun, 192311002 Divya]
```

---

## 3. How HashSet Locates Elements: The Contract Mechanics
`HashSet` is backed by a `HashMap<E, Object>`.
When `set.add(element)` or `set.contains(element)` is called:
1. **Hash Calculation**: The JVM calculates the 32-bit hash value: `h = element.hashCode() ^ (h >>> 16)`.
2. **Bucket Index Resolution**: The bucket index in the internal table array is determined by bitwise masking:
   $$\text{index} = (\text{table.length} - 1) \ \& \ h$$
3. **Bucket Inspection**:
   - If the bucket is empty, the node is inserted directly.
   - If nodes already exist in the bucket, Java traverses the bucket chain (LinkedList or Red-Black Treebin).
   - For each candidate node `p`, it checks:
     $$(p.hash == hash) \ \&\& \ ((p.key == key) \ || \ (key \ne null \ \&\& \ key.equals(p.key)))$$
4. **The Critical Failure Point**:
   If two objects are logically equal according to `equals()` but have **different hash codes** (inheriting identity hash codes from `Object.hashCode()`), they calculate **different bucket indices**. 
   Java looks in the wrong bucket, never encounters the existing object, and never even executes `equals()`. Hence, duplicate records are silently inserted!

---

## 4. Three Deliberately Designed Test Cases

| Test Case | Scenario Description | Prediction | Actual Size | Outcome & Reasoning |
| :--- | :--- | :--- | :--- | :--- |
| **Test Case 1** | Identical object reference (`s1`) re-inserted twice without overriding `hashCode()`. | Size = 1 | 1 | **Pass (Illusion of Correctness)**: `HashSet` checks reference equality `(p.key == key)` first. Because `s1 == s1`, the duplicate is rejected even with broken hash codes. |
| **Test Case 2** | Two distinct `Student` instances with identical `regNo` (`192311005`) without `hashCode()`. | Size = 2 | 2 | **Pass (Duplicate Exposed)**: Distinct heap instances receive different system identity hashes, mapping to separate buckets. `equals()` is never called, and duplicate entries are retained. |
| **Test Case 3** | Mutating `regNo` of an inserted object (`192311010` $\to$ `192311999`). | `contains()` = `false`, Size on re-add = 2 | `false`, Size = 2 | **Pass (Key Mutation Hazard)**: Mutating key fields alters the hash code. `contains()` looks in the new bucket and finds it empty (phantom memory leak). Re-adding puts the exact same instance into two different buckets! |

---

## 5. Alternative Implementations & Complexity Analysis

### Alternative 1: `TreeSet<Student>` with `Comparator`
- **Implementation**: `Set<Student> treeSet = new TreeSet<>(Comparator.comparing(Student::getRegNo));`
- **Mechanics**: Does not use `hashCode()` or `equals()`. Navigates a self-balancing Red-Black Tree, treating elements as duplicates if and only if `comparator.compare(a, b) == 0`.
- **Time Complexity**: $O(\log N)$ for scan insertion and lookup.
- **Space Complexity**: $O(N)$ with 32–40 bytes per `TreeMap.Entry` node.

### Alternative 2: `HashMap<String, Student>` Keyed by `regNo`
- **Implementation**: `map.putIfAbsent(student.getRegNo(), student);`
- **Mechanics**: Uses `String`'s immutable, cached `hashCode()` and `equals()`. Separates the key from the entity, guaranteeing safety against entity mutation.
- **Time Complexity**: $O(1)$ average time per scan.
- **Space Complexity**: $O(N)$ with HashMap node overhead.

---

## 6. Compilation and Execution
```bash
# From repository root:
javac -d . Task03_HashSetContract/src/*.java
java Task03_HashSetContract.src.AttendanceOriginal
java Task03_HashSetContract.src.Attendance
java Task03_HashSetContract.src.ContractTestCases
java Task03_HashSetContract.src.AlternativeDeDuplicationDemo
```

