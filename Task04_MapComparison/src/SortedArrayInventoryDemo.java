package Task04_MapComparison.src;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Task 04: Alternative Concept - Sorted Array Inventory with Arrays.binarySearch()
 * Compares lookup, insertion, and memory footprint against Map implementations.
 */
public class SortedArrayInventoryDemo {

    static class MedicineRecord implements Comparable<MedicineRecord> {
        final String code;
        final String name;
        int quantity;

        MedicineRecord(String code, String name, int quantity) {
            this.code = code;
            this.name = name;
            this.quantity = quantity;
        }

        @Override
        public int compareTo(MedicineRecord o) {
            return this.code.compareTo(o.code);
        }

        @Override
        public String toString() {
            return code + ":" + name + "(" + quantity + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 04: ALTERNATIVE CONCEPT - SORTED ARRAY & Arrays.binarySearch()");
        System.out.println("================================================================================\n");

        MedicineRecord[] inventory = {
            new MedicineRecord("MED-0099", "Aspirin", 120),
            new MedicineRecord("MED-0012", "Paracetamol", 350),
            new MedicineRecord("MED-0540", "Amoxicillin", 80),
            new MedicineRecord("MED-0230", "Ibuprofen", 210),
            new MedicineRecord("MED-0105", "Cetirizine", 95)
        };

        // 1. Sort the array
        Arrays.sort(inventory);
        System.out.println("Sorted Array Inventory:");
        for (MedicineRecord m : inventory) {
            System.out.println("   " + m);
        }
        System.out.println();

        // 2. Perform Binary Search
        String searchTarget = "MED-0230";
        MedicineRecord key = new MedicineRecord(searchTarget, "", 0);
        int index = Arrays.binarySearch(inventory, key);

        System.out.println("Searching for code: " + searchTarget);
        if (index >= 0) {
            System.out.println("   Found at index " + index + ": " + inventory[index]);
        } else {
            System.out.println("   Item not found (Insertion point: " + (-index - 1) + ")");
        }

        System.out.println("\nComplexity Profile:");
        System.out.println("   - Lookup: O(log N) comparisons via binary search.");
        System.out.println("   - Insertion: O(N) due to finding insertion index and shifting elements right.");
        System.out.println("   - Sorted Code Report: O(N) contiguous iteration, zero sorting needed!");
        System.out.println("   - Space: Ultra-compact (only 1 reference per element in array), 0 node wrapper objects.");
    }
}

