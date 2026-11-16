package Task03_HashSetContract.src;

import java.util.HashSet;
import java.util.Set;

/**
 * Task 03: Supplied Attendance Program - BEFORE FIXING TODO 1
 * Demonstrates the broken state where hashCode() is not overridden.
 */
class StudentBroken {
    private final String regNo;
    private final String name;

    StudentBroken(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentBroken)) return false;
        return regNo.equals(((StudentBroken) o).regNo);
    }

    // TODO 1: hashCode() is NOT overridden here!
    // Inherits Object.hashCode() (system identity hash code)

    @Override
    public String toString() {
        return regNo + " " + name;
    }
}

public class AttendanceOriginal {
    public static void main(String[] args) {
        System.out.println("=== ATTENDANCE PROGRAM (BEFORE FIXING TODO 1) ===");
        Set<StudentBroken> present = new HashSet<>();
        present.add(new StudentBroken("192311001", "Arun"));    // scanned at Gate 1
        present.add(new StudentBroken("192311002", "Divya"));
        present.add(new StudentBroken("192311001", "Arun"));    // scanned again at Gate 3

        System.out.println("Unique students: " + present.size());
        System.out.println("Students in set: " + present);
    }
}

