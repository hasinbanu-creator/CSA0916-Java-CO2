package Task03_HashSetContract.src;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Task 03: Supplied Attendance Program - AFTER FIXING TODO 1
 * Demonstrates the corrected Student class adhering to the hashCode-equals contract.
 */
class Student {
    private final String regNo;
    private final String name;

    Student(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        return regNo.equals(((Student) o).regNo);
    }

    // TODO 1: Override hashCode() so that it is consistent with equals()
    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }

    public String getRegNo() {
        return regNo;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return regNo + " " + name;
    }
}

public class Attendance {
    public static void main(String[] args) {
        System.out.println("=== ATTENDANCE PROGRAM (AFTER FIXING TODO 1) ===");
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun"));    // scanned at Gate 1
        present.add(new Student("192311002", "Divya"));
        present.add(new Student("192311001", "Arun"));    // scanned again at Gate 3

        System.out.println("Unique students: " + present.size());
        System.out.println("Students in set: " + present);
    }
}

