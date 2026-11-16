package Task03_HashSetContract.src;

import java.util.*;

/**
 * Task 03: Alternative Deduplication Approaches
 * Demonstrates TreeSet with Comparator and HashMap keyed by regNo.
 */
public class AlternativeDeDuplicationDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 03: ALTERNATIVE DEDUPLICATION ARCHITECTURES");
        System.out.println("================================================================================\n");

        Student[] rawScans = {
            new Student("192311001", "Arun"),
            new Student("192311002", "Divya"),
            new Student("192311001", "Arun"),
            new Student("192311003", "Karthik"),
            new Student("192311002", "Divya")
        };

        // Alternative 1: TreeSet with Comparator on regNo
        System.out.println("1. Alternative 1: TreeSet<Student> with Comparator.comparing(Student::getRegNo)");
        System.out.println("   - Does NOT rely on hashCode() or equals(); relies strictly on Comparator returning 0.");
        Set<Student> treeSet = new TreeSet<>(Comparator.comparing(Student::getRegNo));
        for (Student s : rawScans) treeSet.add(s);
        System.out.println("   Unique count: " + treeSet.size());
        System.out.println("   Natural sorted order maintained: " + treeSet);
        System.out.println("   Time Complexity per scan: O(log N) (Red-Black self-balancing tree)");
        System.out.println();

        // Alternative 2: HashMap<String, Student> keyed by regNo
        System.out.println("2. Alternative 2: Map<String, Student> keyed by String regNo");
        System.out.println("   - Uses String's built-in, highly optimized hashCode() and equals().");
        System.out.println("   - Automatically overwrites/preserves existing entry on duplicate key.");
        Map<String, Student> studentMap = new HashMap<>();
        for (Student s : rawScans) {
            studentMap.putIfAbsent(s.getRegNo(), s);
        }
        System.out.println("   Unique count: " + studentMap.size());
        System.out.println("   Values: " + studentMap.values());
        System.out.println("   Time Complexity per scan: O(1) average");
        System.out.println();

        System.out.println("Deduplication alternative demonstration completed.");
    }
}

