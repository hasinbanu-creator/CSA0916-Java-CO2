package Task03_HashSetContract.src;

import java.util.HashSet;
import java.util.Set;

/**
 * Task 03: Three Deliberately Designed Test Cases
 * Validates predictions and explores edge cases of the hashCode-equals contract.
 */
public class ContractTestCases {

    // Helper mutable student class for Test 3
    static class MutableStudent {
        String regNo;
        String name;

        MutableStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MutableStudent)) return false;
            return regNo.equals(((MutableStudent) o).regNo);
        }

        @Override
        public int hashCode() {
            return regNo != null ? regNo.hashCode() : 0;
        }

        @Override
        public String toString() {
            return regNo + " " + name;
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 03: THREE DELIBERATELY DESIGNED TEST CASES");
        System.out.println("================================================================================\n");

        // --- TEST CASE 1: HashSet Appears to Work Correctly ---
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST CASE 1: Identical Object Reference Re-inserted (Broken Student)");
        System.out.println("Prediction: Size = 1");
        System.out.println("Reason: When the exact same reference (s1) is added again, HashSet's internal");
        System.out.println("        HashMap checks reference equality (p.hash == hash && ((k = p.key) == key)).");
        System.out.println("        Because (s1 == s1) evaluates to true, the duplicate is rejected even");
        System.out.println("        without overriding hashCode(), creating a false illusion of correctness!");

        Set<StudentBroken> set1 = new HashSet<>();
        StudentBroken s1 = new StudentBroken("192311001", "Arun");
        set1.add(s1);
        set1.add(s1); // Re-inserting exact same reference
        System.out.println("Actual Size: " + set1.size());
        System.out.println("Match Prediction? " + (set1.size() == 1 ? "YES" : "NO"));
        System.out.println();

        // --- TEST CASE 2: Deliberately Expose Duplicate Entries ---
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST CASE 2: Two Distinct Objects with Identical regNo (Broken Student)");
        System.out.println("Prediction: Size = 2");
        System.out.println("Reason: s2a and s2b are distinct heap allocations. Default Object.hashCode() uses");
        System.out.println("        system identity hash codes (typically memory address derived).");
        System.out.println("        s2a and s2b almost certainly land in different hash buckets.");
        System.out.println("        HashSet never invokes equals() because bucket bins do not collide!");

        Set<StudentBroken> set2 = new HashSet<>();
        StudentBroken s2a = new StudentBroken("192311005", "Kavya");
        StudentBroken s2b = new StudentBroken("192311005", "Kavya");
        set2.add(s2a);
        set2.add(s2b);
        System.out.println("Actual Size: " + set2.size());
        System.out.println("Contents: " + set2);
        System.out.println("Match Prediction? " + (set2.size() == 2 ? "YES (Duplicate entry exposed!)" : "NO"));
        System.out.println();

        // --- TEST CASE 3: Mutating Hash Key Field After Insertion ---
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("TEST CASE 3: Mutating Field Used in hashCode() After Insertion");
        System.out.println("Prediction: After mutation, contains() returns FALSE for the same object, and");
        System.out.println("            re-adding the same object results in Size = 2!");
        System.out.println("Reason: When s3 is added with regNo '192311010', it is placed into bucket B1 based on");
        System.out.println("        hashCode('192311010'). When regNo is changed to '192311999', its hashCode");
        System.out.println("        changes. contains() calculates bucket B2 based on the new hash, finds B2 empty,");
        System.out.println("        and declares the object absent. Adding it again stores it in B2, resulting in");
        System.out.println("        a duplicate corrupted set containing the same object twice!");

        Set<MutableStudent> set3 = new HashSet<>();
        MutableStudent s3 = new MutableStudent("192311010", "Rahul");
        set3.add(s3);
        System.out.println("Initial Size: " + set3.size() + " | Contains s3 ('192311010')? " + set3.contains(s3));

        // Mutating key field
        s3.regNo = "192311999";
        System.out.println("After mutating regNo to '192311999':");
        boolean containsAfterMutation = set3.contains(s3);
        System.out.println("   set.contains(s3): " + containsAfterMutation + " (Phantom memory leak / unfindable!)");

        set3.add(s3); // Re-adding the mutated object
        System.out.println("After re-adding s3 to set: Actual Size = " + set3.size());
        System.out.println("Match Prediction? " + (!containsAfterMutation && set3.size() == 2 ? "YES (Key mutation hazard confirmed!)" : "NO"));
        System.out.println("--------------------------------------------------------------------------------");
    }
}

