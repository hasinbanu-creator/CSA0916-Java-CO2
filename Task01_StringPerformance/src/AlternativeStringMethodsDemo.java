package Task01_StringPerformance.src;

import java.util.List;
import java.util.StringJoiner;

/**
 * Task 01: Alternative Concepts Demo
 * Demonstrates String.join(), StringJoiner, and String.format()
 * as modern alternatives to manual StringBuilder loops.
 */
public class AlternativeStringMethodsDemo {

    public static String buildUsingStringJoin(List<String> fields) {
        return String.join("\n", fields) + "\n";
    }

    public static String buildUsingStringJoiner(List<String> fields) {
        StringJoiner sj = new StringJoiner("\n", "--- RAILWAY E-TICKET ---\n", "\n------------------------");
        for (String field : fields) {
            sj.add(field);
        }
        return sj.toString();
    }

    public static String buildUsingStringFormat(List<String> fields) {
        // String.format parses format specifiers at runtime, adding parsing overhead
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(String.format("%s%n", field));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  TASK 01: ALTERNATIVE CONCEPTS (String.join, StringJoiner, String.format)");
        System.out.println("================================================================================\n");

        List<String> sampleFields = RailwayTicketBenchmark.generateFields(5);

        System.out.println("1. Output from String.join():");
        System.out.println(buildUsingStringJoin(sampleFields));

        System.out.println("2. Output from StringJoiner (with prefix and suffix):");
        System.out.println(buildUsingStringJoiner(sampleFields));

        System.out.println("3. Output from String.format():");
        System.out.println(buildUsingStringFormat(sampleFields));

        System.out.println("Execution completed successfully.");
    }
}

